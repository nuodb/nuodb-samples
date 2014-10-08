/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.dbapi;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.DeserializationConfig;

import com.nuodb.storefront.exception.ApiProxyException;
import com.nuodb.storefront.exception.ApiUnavailableException;
import com.nuodb.storefront.exception.DataValidationException;
import com.nuodb.storefront.exception.DatabaseNotFoundException;
import com.nuodb.storefront.model.dto.DbConnInfo;
import com.nuodb.storefront.model.dto.DbFootprint;
import com.nuodb.storefront.model.dto.RegionStats;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.Base64;

public class DbApiProxy implements IDbApi {
    private static final ClientConfig s_cfg = new DefaultClientConfig();
    private static final Logger s_logger = Logger.getLogger(DbApiProxy.class.getName());

    private static final String DBVAR_TE_HOST_TAG = "TE_HOST_TAG";
    private static final String DBVAR_SM_HOST_TAG = "SM_HOST_TAG";
    private static final String DBVAR_SM_MAX = "SM_MAX";
    private static final String DBVAR_HOST = "HOST";
    private static final String DBVAR_REGION = "REGION";

    private final String baseUrl;
    private final String authHeader;
    private final String apiUsername;
    private final DbConnInfo dbConnInfo;

    static {
        s_cfg.getSingletons().add(new JacksonJaxbJsonProvider().configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false));
    }

    public DbApiProxy(String baseUrl, String apiUsername, String apiPassword, DbConnInfo dbConnInfo) {
        this.baseUrl = baseUrl;
        this.apiUsername = apiUsername;
        this.authHeader = "Basic " + new String(Base64.encode(apiUsername + ":" + apiPassword));
        this.dbConnInfo = dbConnInfo;
    }

    @Override
    public String getBaseUrl() {
        return baseUrl;
    }

    @Override
    public String getAuthUser() {
        return apiUsername;
    }

    @Override
    public Database getDb() throws ApiProxyException, ApiUnavailableException {
        try {
            String dbName = dbConnInfo.getDbName();
            return Client.create(s_cfg)
                    .resource(baseUrl + "/databases/" + urlEncode(dbName))
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .type(MediaType.APPLICATION_JSON)
                    .get(Database.class);
        } catch (ClientHandlerException e) {
            // DB not found
            return null;
        } catch (Exception e) {
            throw toApiException(e);
        }
    }

    @Override
    public List<Process> getDbProcesses() {
        try {
            List<Process> processes = new ArrayList<Process>();

            List<Region> regions = getRegions();
            String dbName = dbConnInfo.getDbName();
            Map<String, String> processRegionNameMap = new HashMap<String, String>();

            // Map process UIDs to region names
            for (Region region : regions) {
                for (int hostIdx = 0; hostIdx < region.hosts.length; hostIdx++) {
                    Host host = region.hosts[hostIdx];
                    for (int processIdx = 0; processIdx < host.processes.length; processIdx++) {
                        Process process = host.processes[processIdx];
                        processRegionNameMap.put(process.uid, region.region);
                    }
                }
            }

            // Extract all processes associated with the target DB
            for (Region region : regions) {
                for (int databaseIdx = 0; databaseIdx < region.databases.length; databaseIdx++) {
                    Database database = region.databases[databaseIdx];
                    if (database != null && dbName.equals(database.name)) {
                        for (int processIdx = 0; processIdx < database.processes.length; processIdx++) {
                            Process process = database.processes[processIdx];
                            process.region = processRegionNameMap.get(process.uid);
                            processes.add(process);
                        }
                        return processes;
                    }
                }
            }

            return processes;
        } catch (Exception e) {
            throw toApiException(e);
        }
    }

    @Override
    public void shutdownProcess(String uid) {
        try {
            Client.create(s_cfg)
                    .resource(baseUrl + "/processes/" + uid)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .type(MediaType.APPLICATION_JSON)
                    .delete();
        } catch (Exception e) {
            throw toApiException(e);
        }
    }

    @Override
    public Database fixDbSetup(boolean createIfDne) {
        // Verify DB exists
        List<Region> regions = getRegions();
        Database db = findStorefrontDatabase(regions);
        if (db == null) {
            if (!createIfDne) {
                throw new DatabaseNotFoundException();
            }
        }

        // Sync footprint & template, ensuring DB is running on at least 1 node
        DbFootprint stats = getDbFootprint(regions);
        setDbFootprint(Math.max(1, stats.usedRegionCount), Math.max(1, stats.usedHostCount), true, regions);

        return db;
    }

    @Override
    public List<RegionStats> getRegionStats() {
        List<Region> regions = getRegions();
        List<RegionStats> stats = new ArrayList<RegionStats>(regions.size());
        for (Region region : regions) {
            stats.add(new RegionStats(region));
        }
        return stats;
    }

    protected List<Region> getRegions() {
        try {
            String dbName = dbConnInfo.getDbName();
            String dbProcessTag = dbConnInfo.getDbProcessTag();

            Region[] regions = Client.create(s_cfg)
                    .resource(baseUrl + "/regions")
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .type(MediaType.APPLICATION_JSON)
                    .get(Region[].class);

            for (Region region : regions) {
                for (Host host : region.hosts) {
                    region.hostCount++;
                    if (host.tags.containsKey(dbProcessTag)) {
                        region.usedHostCount++;
                        for (Process process : host.processes) {
                            if (process.dbname.equals(dbName)) {
                                if ("TE".equals(process.type)) {
                                    region.transactionManagerCount++;
                                } else if ("SM".equals(process.type)) {
                                    region.storageManagerCount++;
                                }
                            }
                        }
                    }
                }
            }

            return Arrays.asList(regions);
        } catch (Exception e) {
            throw toApiException(e);
        }
    }

    @Override
    public DbFootprint getDbFootprint() {
        return getDbFootprint(getRegions());
    }

    @Override
    public synchronized DbFootprint setDbFootprint(int numRegions, int numHosts) {
        return setDbFootprint(numRegions, numHosts, false, getRegions());
    }

    protected DbFootprint setDbFootprint(int numRegions, int numHosts, boolean createIfDne, List<Region> regions) {
        // Validate params
        if (numRegions < 1) {
            throw new DataValidationException("Number of regions must be positive");
        }
        if (numHosts < 1) {
            throw new DataValidationException("Number of regions must be positive");
        }

        try {
            // Determine which regions are currently used
            String dbProcessTag = dbConnInfo.getDbProcessTag();
            List<Region> usedRegions = new ArrayList<Region>();
            List<Region> unusedRegions = new ArrayList<Region>();
            for (Region region : regions) {
                boolean isRegionUsed = false;
                for (Host host : region.hosts) {
                    if (host.tags.containsKey(dbProcessTag)) {
                        isRegionUsed = true;
                        break;
                    }
                }
                if (isRegionUsed) {
                    usedRegions.add(region);
                } else {
                    unusedRegions.add(region);
                }
            }

            // Pick regions to add/remove from used list
            Random rnd = new Random();
            while (usedRegions.size() > numRegions) {
                unusedRegions.add(usedRegions.remove(rnd.nextInt(usedRegions.size())));
            }
            while (!unusedRegions.isEmpty() && usedRegions.size() < numRegions) {
                usedRegions.add(unusedRegions.remove(rnd.nextInt(unusedRegions.size())));
            }

            // Untag all tagged hosts in unused regions
            for (Region unusedRegion : unusedRegions) {
                for (Host unusedHost : unusedRegion.hosts) {
                    if (unusedHost.tags.containsKey(dbProcessTag)) {
                        removeHostTag(unusedHost, dbProcessTag, true);
                    }
                }
                unusedRegion.usedHostCount = 0;
            }

            // Ensure proper number of hosts are tagged in each used region
            Host firstUsedHost = null;
            for (Region usedRegion : usedRegions) {
                List<Host> usedHosts = new ArrayList<Host>();
                List<Host> unusedHosts = new ArrayList<Host>();
                for (Host host : usedRegion.hosts) {
                    if (host.tags.containsKey(dbProcessTag)) {
                        usedHosts.add(host);
                    } else {
                        unusedHosts.add(host);
                    }
                }

                while (usedHosts.size() > numHosts) {
                    Host removedHost = usedHosts.remove(rnd.nextInt(usedHosts.size()));
                    removeHostTag(removedHost, dbProcessTag, false);
                }
                while (!unusedHosts.isEmpty() && usedHosts.size() < numHosts) {
                    Host addedHost = unusedHosts.remove(rnd.nextInt(unusedHosts.size()));
                    addHostTag(addedHost, dbProcessTag, "1");
                    usedHosts.add(addedHost);
                }
                if (firstUsedHost == null && !usedHosts.isEmpty()) {
                    firstUsedHost = usedHosts.get(0);
                }
                usedRegion.usedHostCount = usedHosts.size();
            }

            DbFootprint footprint = getDbFootprint(regions);

            // Update template if current one doesn't match desired footprint
            Database database = findStorefrontDatabase(regions);
            if (database != null || createIfDne) {
                boolean createDb = database == null;
                if (createDb) {
                    database = new Database();
                }
                boolean updateDb = fixDatabaseTemplate(database, footprint.usedRegionCount, footprint.usedHostCount, usedRegions.get(0).region, firstUsedHost.id);
                if (createDb) {
                    database.name = dbConnInfo.getDbName();
                    database.username = dbConnInfo.getUsername();
                    database.password = dbConnInfo.getPassword();

                    s_logger.info("Creating DB '" + database.name + "' with template '" + database.template + "' and vars " + database.variables);
                    Client.create(s_cfg)
                            .resource(baseUrl + "/databases/")
                            .header(HttpHeaders.AUTHORIZATION, authHeader)
                            .type(MediaType.APPLICATION_JSON)
                            .post(Database.class, database);
                } else if (updateDb) {
                    s_logger.info("Updating DB '" + database.name + "' with template '" + database.template + "' and vars " + database.variables);
                    Client.create(s_cfg)
                            .resource(baseUrl + "/databases/" + urlEncode(database.name))
                            .header(HttpHeaders.AUTHORIZATION, authHeader)
                            .type(MediaType.APPLICATION_JSON)
                            .put(Database.class, database);
                }
            }

            return footprint;
        } catch (Exception e) {
            throw toApiException(e);
        }
    }

    protected void addHostTag(Host host, String tagName, String tagValue) {
        s_logger.info("Adding tag '" + tagName + "' to host " + host.address + " (id=" + host.id + ")");

        Tag tag = new Tag();
        tag.key = tagName;
        tag.value = tagValue;

        Client.create(s_cfg)
                .resource(baseUrl + "/hosts/" + host.id + "/tags")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .type(MediaType.APPLICATION_JSON).post(tag);

        host.tags.put(tag.key, tag.value);
    }

    protected void removeHostTag(Host host, String tagName, boolean shutdownSMs) {
        s_logger.info("Removing tag '" + tagName + "' from host " + host.address + " (id=" + host.id + ")");

        Client.create(s_cfg)
                .resource(baseUrl + "/hosts/" + host.id + "/tags/" + urlEncode(tagName))
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .type(MediaType.APPLICATION_JSON).delete();

        host.tags.remove(tagName);

        String dbName = dbConnInfo.getDbName();
        for (Process process : host.processes) {
            if (process.dbname.equals(dbName)) {
                if (shutdownSMs || !"SM".equals(process.type)) {
                    s_logger.info("Shutting down " + process.type + " process on host " + host.address + " (uid=" + process.uid + ")");
                    shutdownProcess(process.uid);
                }
            }
        }
    }

    protected Database findStorefrontDatabase(List<Region> regions) {
        String dbName = dbConnInfo.getDbName();

        for (Region region : regions) {
            for (Database database : region.databases) {
                if (database != null && database.name.equals(dbName)) {
                    return database;
                }
            }
        }

        return null;
    }

    protected DbFootprint getDbFootprint(List<Region> regions) {
        DbFootprint dbStats = new DbFootprint();
        dbStats.regionCount = regions.size();

        for (Region region : regions) {
            dbStats.hostCount = Math.max(region.hostCount, dbStats.hostCount);
            dbStats.usedHostCount = Math.max(region.usedHostCount, dbStats.usedHostCount);
            if (region.usedHostCount > 0) {
                dbStats.usedRegions.add(region.region);
                dbStats.usedRegionCount++;
            }
        }

        return dbStats;
    }

    protected ApiProxyException toApiException(Exception e) {
        if (e instanceof ClientHandlerException) {
            return new ApiUnavailableException((ClientHandlerException) e);
        }
        if (e instanceof UniformInterfaceException) {
            return new ApiProxyException(((UniformInterfaceException) e).getResponse(), e);
        }
        return new ApiProxyException(Status.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }

    @SuppressWarnings("unchecked")
    protected boolean fixDatabaseTemplate(Database database, int targetRegions, int targetHosts, String firstRegion, String firstHostId) {
        // Initialize DB variable map
        Map<String, String> vars = new HashMap<String, String>();

        // Specify host tags for SMs and TEs
        String dbProcessTag = dbConnInfo.getDbProcessTag();
        vars.put(DBVAR_SM_HOST_TAG, dbProcessTag);
        vars.put(DBVAR_TE_HOST_TAG, dbProcessTag);

        // Determine which template to use, and add template-specific variables
        String templateName;
        if (targetRegions > 1) {
            templateName = "Geo-distributed";
            vars.put(DBVAR_REGION, null);
            vars.put(DBVAR_HOST, null);
            vars.put(DBVAR_SM_MAX, null);
        } else if (targetHosts > 1) {
            templateName = "Multi Host";
            vars.put(DBVAR_REGION, firstRegion);
            vars.put(DBVAR_HOST, null);
            vars.put(DBVAR_SM_MAX, "1");
        } else {
            templateName = "Single Host";
            vars.put(DBVAR_REGION, null);
            vars.put(DBVAR_HOST, firstHostId);
            vars.put(DBVAR_SM_MAX, null);
        }

        // Apply template name
        int changeCount = 0;
        String oldTemplateName = null;
        if (database.template instanceof Map) {
            oldTemplateName = ((Map<String, String>) database.template).get("name");
        } else if (database.template != null) {
            oldTemplateName = String.valueOf(database.template);
        }
        if (!templateName.equals(oldTemplateName)) {
            changeCount++;
        }
        database.template = templateName;  // always set DB template to a string since any update request needs this as a string, not an object

        // Apply variables
        if (database.variables == null) {
            database.variables = new HashMap<String, String>();
        }
        for (Map.Entry<String, String> varPair : vars.entrySet()) {
            if (varPair.getValue() == null) {
                if (database.variables.remove(varPair.getKey()) != null) {
                    changeCount++;
                }
            } else {
                if (!varPair.getValue().equals(database.variables.put(varPair.getKey(), varPair.getValue()))) {
                    changeCount++;
                }
            }
        }

        return changeCount > 0;
    }

    private static final String urlEncode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
