/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.service.storefront;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;

import com.nuodb.storefront.StorefrontApp;
import com.nuodb.storefront.StorefrontTenantManager;
import com.nuodb.storefront.dal.IStorefrontDao;
import com.nuodb.storefront.dal.TransactionType;
import com.nuodb.storefront.exception.ApiException;
import com.nuodb.storefront.model.dto.ConnInfo;
import com.nuodb.storefront.model.dto.DbRegionInfo;
import com.nuodb.storefront.model.dto.RegionStats;
import com.nuodb.storefront.model.entity.AppInstance;
import com.nuodb.storefront.service.IHeartbeatService;
import com.nuodb.storefront.service.IStorefrontPeerService;
import com.nuodb.storefront.service.IStorefrontTenant;
import com.nuodb.storefront.util.PerformanceUtil;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.uri.UriComponent;
import com.sun.jersey.api.uri.UriComponent.Type;

public class HeartbeatService implements IHeartbeatService, IStorefrontPeerService {
    private final Logger logger;
    private final IStorefrontTenant tenant;
    private int secondsUntilNextPurge = 0;
    private int consecutiveFailureCount = 0;
    private int successCount = 0;
    private Map<String, Set<URI>> wakeList = new HashMap<String, Set<URI>>();
    private Set<URI> warnList = new HashSet<URI>();
    private long cumGcTime = 0;

    public HeartbeatService(IStorefrontTenant tenant) {
        this.tenant = tenant;
        this.logger = tenant.getLogger(getClass());
    }

    @Override
    public void run() {
        try {            
            final IStorefrontDao dao = tenant.createStorefrontDao();
            dao.runTransaction(TransactionType.READ_WRITE, "sendHeartbeat", new Runnable() {
                @Override
                public void run() {
                    Calendar now = Calendar.getInstance();
                    AppInstance appInstance = tenant.getAppInstance();
                    secondsUntilNextPurge -= StorefrontApp.HEARTBEAT_INTERVAL_SEC;

                    if (appInstance.getFirstHeartbeat() == null) {
                        appInstance.setFirstHeartbeat(now);
                        appInstance.setLastApiActivity(now);
                    }

                    // Send the heartbeat with the latest "last heartbeat time"
                    DbRegionInfo region = dao.getCurrentDbNodeRegion();
                    appInstance.setCpuUtilization(PerformanceUtil.getAvgCpuUtilization());
                    appInstance.setLastHeartbeat(now);
                    appInstance.setRegion(region.regionName);
                    appInstance.setNodeId(region.nodeId);
                    dao.save(appInstance); // this will create or update as appropriate

                    // If enough time has elapsed, also delete rows of instances that are no longer sending heartbeats
                    if (secondsUntilNextPurge <= 0) {
                        Calendar maxLastHeartbeat = Calendar.getInstance();
                        maxLastHeartbeat.add(Calendar.SECOND, -StorefrontApp.MIN_INSTANCE_PURGE_AGE_SEC);
                        dao.deleteDeadAppInstances(maxLastHeartbeat);
                        secondsUntilNextPurge = StorefrontApp.PURGE_FREQUENCY_SEC;
                    }

                    // If interactive user has left the app, shut down any active workloads
                    Calendar idleThreshold = Calendar.getInstance();
                    idleThreshold.add(Calendar.SECOND, -StorefrontApp.STOP_USERS_AFTER_IDLE_UI_SEC);
                    if (appInstance.getStopUsersWhenIdle() && appInstance.getLastApiActivity().before(idleThreshold)) {
                        // Don't do any heavy lifting if there are no simulated workloads in progress
                        int activeWorkerCount = tenant.getSimulatorService().getActiveWorkerLimit();
                        if (activeWorkerCount > 0) {
                            // Check for idleness across *all* instances
                            if (dao.getActiveAppInstanceCount(idleThreshold) == 0) {
                                logger.info(appInstance.getTenantName() + ": Stopping all " + activeWorkerCount
                                        + " simulated users due to idle app instances.");
                                tenant.getSimulatorService().stopAll();
                            }
                        }
                    } else {
                        // We're still active, so if there are Storefronts to wake up, let's do it
                        wakeStorefronts();
                    }

                    consecutiveFailureCount = 0;
                    successCount++;
                }
            });
            
            long gcTime = PerformanceUtil.getGarbageCollectionTime();
            if (gcTime > cumGcTime + StorefrontApp.GC_CUMULATIVE_TIME_LOG_MS) {
                logger.info("Cumulative GC time of " + gcTime + " ms");
                cumGcTime = gcTime;
            }
        } catch (Exception e) {
            if (successCount > 0 && ++consecutiveFailureCount == 1) {
                logger.error(tenant.getAppInstance().getTenantName() + ": Unable to send heartbeat", e);
            }
        }
    }

    @Override
    public void asyncWakeStorefrontsInOtherRegions() {
        if (tenant != StorefrontTenantManager.getDefaultTenant()) {
            // Don't wake other Storefronts if we're not the default tenant. We don't want to presume the other tenants can/should have the same
            // Storefront footprint as the default tenant.
            return;
        }

        // Assume no regions are covered
        Collection<RegionStats> regions = tenant.getDbApi().getRegionStats();
        Map<String, RegionStats> missingRegions = new HashMap<String, RegionStats>();
        for (RegionStats region : regions) {
            if (region.usedHostCount > 0) {
                missingRegions.put(region.region, region);
            }
        }

        if (regions.size() <= 1) {
            // When there's only 1 region, we're in it -- so there's no work to do
            return;
        }

        // Eliminate regions that are covered by existing active instances
        List<AppInstance> instances = tenant.createStorefrontService().getAppInstances(true);
        for (AppInstance instance : instances) {
            missingRegions.remove(instance.getRegion());
        }

        // Queue up candidate URLs of storefronts in regions that are not covered
        synchronized (wakeList) {
            // Discard prior data. We've now got the latest across all regions.
            wakeList.clear();

            for (RegionStats region : missingRegions.values()) {
                // Put the URIs in a *sorted* set so all active Storefronts hit these in a deterministic order.
                // Otherwise they may wake multiple Storefronts in a region, which isn't bad but unnecessary.
                wakeList.put(region.region, new TreeSet<URI>(region.usedHostUrls));
            }
        }
    }

    protected void wakeStorefronts() {
        // See if there's anything in the list to wake
        HashMap<String, Set<URI>> wakeListCopy;
        synchronized (wakeList) {
            if (wakeList.isEmpty()) {
                return;
            }
            wakeListCopy = new HashMap<String, Set<URI>>(wakeList);
        }

        // Get the best known scheme, port, and path for this Storefront instance.
        // We'll assume the others are running with the same settings.
        String sfScheme;
        int sfPort;
        String sfPath;
        try {
            URI homeUrl = new URI(tenant.getAppInstance().getUrl());
            sfScheme = homeUrl.getScheme();
            sfPort = homeUrl.getPort();
            sfPath = homeUrl.getPath();
            if (sfPath.endsWith("/")) {
                sfPath = sfPath.substring(0, sfPath.length() - 1);
            }
        } catch (URISyntaxException e1) {
            return;
        }

        String tenantName = tenant.getAppInstance().getTenantName();

        // Wake 1 Storefront in each region
        Client client = tenant.createApiClient();
        for (Map.Entry<String, Set<URI>> entry : wakeListCopy.entrySet()) {
            String region = entry.getKey();
            for (URI peerHostUrl : entry.getValue()) {
                URI peerStorefrontUrl;
                try {
                    peerStorefrontUrl = new URI(sfScheme, null, peerHostUrl.getHost(), sfPort, sfPath + "/api/app-instances/sync",
                            "tenant=" + UriComponent.encode(tenantName, Type.QUERY_PARAM), null);
                } catch (URISyntaxException e1) {
                    continue;
                }

                try {
                    client.resource(peerStorefrontUrl)
                            .type(MediaType.APPLICATION_JSON)
                            .put(ConnInfo.class, tenant.getDbConnInfo());
                    logger.info(tenantName + ": Successfully contacted peer Storefront at [" + peerStorefrontUrl + "] in the " + region + " region.");

                    // Success. We're done in this region.
                    break;
                } catch (Exception e) {
                    synchronized (warnList) {
                        boolean warn = false;
                        if (!warnList.contains(peerStorefrontUrl)) {
                            warnList.add(peerStorefrontUrl);
                            warn = true;
                        }
                        if (warn) {
                            ApiException ae = ApiException.toApiException(e);
                            logger.warn(tenantName + ": Unable to contact peer Storefront [" + peerStorefrontUrl + "] in the " + region + " region: "
                                    + ae.getMessage());
                        }
                    }
                }

                synchronized (wakeList) {
                    wakeList.remove(peerHostUrl);
                }
            }
        }
    }
}
