/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.dbapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;

import com.nuodb.storefront.StorefrontApp;
import com.nuodb.storefront.StorefrontFactory;
import com.nuodb.storefront.exception.ApiException;
import com.nuodb.storefront.exception.DataValidationException;
import com.nuodb.storefront.exception.DatabaseNotFoundException;
import com.nuodb.storefront.model.dto.ConnInfo;
import com.nuodb.storefront.model.dto.DbConnInfo;
import com.nuodb.storefront.model.dto.DbFootprint;
import com.nuodb.storefront.model.dto.RegionStats;
import com.nuodb.storefront.util.NetworkUtil;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.uri.UriComponent;
import com.sun.jersey.api.uri.UriComponent.Type;
import com.sun.jersey.core.util.Base64;

public class DbApiProxy implements IDbApi {
    private static final Logger s_logger = Logger.getLogger(DbApiProxy.class.getName());

    private static final String DBVAR_TAG_CONSTRAINT_GROUP_TE = "TEs";
    private static final String DBVAR_TAG_CONSTRAINT_GROUP_SM = "SMs";
    private static final String DBVAR_TAG_EXISTS_CONSTRAINT = "ex:";
    private static final String DBVAR_SM_MIN = "SM_MIN";
    private static final String DBVAR_SM_MAX = "SM_MAX";
    private static final String DBVAR_TE_MIN = "TE_MIN";
    private static final String DBVAR_HOST = "HOST";
    private static final String DBVAR_REGION = "REGION";

    private static final String TEMPLATE_GEO_DISTRIBUTED = "Geo-distributed";
    private static final String TEMPLATE_MULTI_HOST = "Multi Host";
    private static final String TEMPLATE_SINGLE_HOST = "Single Host";

    private static final String PROCESS_TYPE_TE = "TE";
    private static final String PROCESS_TYPE_SM = "SM";

    private final ConnInfo apiConnInfo;
    private final DbConnInfo dbConnInfo;

    public DbApiProxy(ConnInfo apiConnInfo, DbConnInfo dbConnInfo) {
        this.apiConnInfo = apiConnInfo;
        this.dbConnInfo = dbConnInfo;
    }

    @Override
    public ConnInfo getApiConnInfo() {
        ConnInfo info = new ConnInfo(apiConnInfo);
        info.setPassword(null);
        return info;
    }

    @Override
    public void testConnection() {
        try {
            buildClient("/templates").get(Object.class);
        } catch (Exception e) {
            throw ApiException.toApiException(e);
        }
    }

    @Override
    public Database getDb() throws ApiException {
        try {
            String dbName = dbConnInfo.getDbName();
            return buildClient("/databases/" + urlEncodePathSegment(dbName)).get(Database.class);
        } catch (ClientHandlerException e) {
            // DB not found
            return null;
        } catch (Exception e) {
            throw ApiException.toApiException(e);
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
            throw ApiException.toApiException(e);
        }
    }

    @Override
    public void shutdownProcess(String uid) {
        try {
            buildClient("/processes/" + uid).delete();
        } catch (Exception e) {
            ApiException ape = ApiException.toApiException(e);
            if (ape.getErrorCode() != Status.NOT_FOUND) {
                throw ape;
            }
        }
    }

    @Override
    public synchronized Database fixDbSetup(boolean createIfDne) {
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

    @Override
    public DbFootprint getDbFootprint() {
        return getDbFootprint(getRegions());
    }

    @Override
    public synchronized DbFootprint setDbFootprint(int numRegions, int numHosts) {
        return setDbFootprint(numRegions, numHosts, false, getRegions());
    }

    protected List<Region> getRegions() {
        try {
            String dbName = dbConnInfo.getDbName();
            String dbProcessTag = dbConnInfo.getDbProcessTag();
            Region[] regions = buildClient("/regions").get(Region[].class);

            for (Region region : regions) {
                for (Host host : region.hosts) {
                    region.hostCount++;
                    if (host.tags.containsKey(dbProcessTag)) {
                        region.usedHostCount++;
                        for (Process process : host.processes) {
                            if (process.dbname.equals(dbName)) {
                                if (PROCESS_TYPE_TE.equals(process.type)) {
                                    region.transactionManagerCount++;
                                } else if (PROCESS_TYPE_SM.equals(process.type)) {
                                    region.storageManagerCount++;
                                }
                            }
                        }
                    }
                }
            }

            return Arrays.asList(regions);
        } catch (Exception e) {
            throw ApiException.toApiException(e);
        }
    }

    protected DbFootprint setDbFootprint(int numRegions, int numHosts, boolean createIfDne, List<Region> regions) {
        // Validate params
        if (numRegions < 1) {
            throw new DataValidationException("Number of regions must be positive");
        }
        if (numHosts < 1) {
            throw new DataValidationException("Number of hosts must be positive");
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

            // Ensure home region is used
            HomeHostInfo homeHostInfo = findHomeHostInfo(regions);
            if (unusedRegions.remove(homeHostInfo.region)) {
                usedRegions.add(homeHostInfo.region);
            }

            // Pick regions to remove from used list (but home region is not eligible for removal)
            Random rnd = new Random();
            while (usedRegions.size() > numRegions) {
                int idx = rnd.nextInt(usedRegions.size());
                if (usedRegions.get(idx) != homeHostInfo.region) {
                    unusedRegions.add(usedRegions.remove(idx));
                }
            }

            // Pick regions to add to the list
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
            for (Region usedRegion : usedRegions) {
                List<Host> usedHosts = new ArrayList<Host>();
                List<Host> unusedHosts = new ArrayList<Host>();
                for (Host host : usedRegion.hosts) {
                    if (host.tags.containsKey(dbProcessTag) || host == homeHostInfo.host) {
                        usedHosts.add(host);
                    } else {
                        unusedHosts.add(host);
                    }
                }

                while (usedHosts.size() > numHosts) {
                    Host removedHost = usedHosts.get(rnd.nextInt(usedHosts.size()));
                    if (removedHost != homeHostInfo.host) {
                        usedHosts.remove(removedHost);
                        removeHostTag(removedHost, dbProcessTag, false);
                    }
                }
                for (Host existingHost : usedHosts) {
                    addHostTag(existingHost, dbProcessTag, "1");
                }
                while (!unusedHosts.isEmpty() && usedHosts.size() < numHosts) {
                    Host addedHost = unusedHosts.remove(rnd.nextInt(unusedHosts.size()));
                    addHostTag(addedHost, dbProcessTag, "1");
                    usedHosts.add(addedHost);
                }
                usedRegion.usedHostCount = usedHosts.size();
            }

            DbFootprint footprint = getDbFootprint(regions);

            // Update template if current one doesn't match desired footprint
            Database database = findStorefrontDatabase(regions);
            if (database != null || createIfDne) {
                if (homeHostInfo.host == null) {
                    throw new DataValidationException("No hosts available to run database.");
                }
                boolean createDb = database == null;
                if (createDb) {
                    database = new Database();
                }
                boolean updateDb = fixDatabaseTemplate(database, footprint.usedRegionCount, footprint.usedHostCount, homeHostInfo);
                if (createDb) {
                    database.name = dbConnInfo.getDbName();
                    database.username = dbConnInfo.getUsername();
                    database.password = dbConnInfo.getPassword();

                    s_logger.info("Creating DB '" + database.name + "' with template '" + database.template + "' and vars " + database.variables);
                    buildClient("/databases/").post(Database.class, database);
                } else if (updateDb) {
                    s_logger.info("Updating DB '" + database.name + "' with template '" + database.template + "' and vars " + database.variables);
                    buildClient("/databases/" + urlEncodePathSegment(database.name)).put(Database.class, database);
                }
            }

            return footprint;
        } catch (Exception e) {
            throw ApiException.toApiException(e);
        }
    }

    protected void addHostTag(Host host, String tagName, String tagValue) {
        String oldTagValue = host.tags.get(tagName);
        if (tagValue.equals(oldTagValue)) {
            // Tag already exists
            return;
        }

        s_logger.info("Adding tag '" + tagName + "' to host " + host.address + " (id=" + host.id + ")");

        Tag tag = new Tag();
        tag.key = tagName;
        tag.value = tagValue;

        buildClient("/hosts/" + host.id + "/tags").post(tag);

        host.tags.put(tag.key, tag.value);
    }

    protected void removeHostTag(Host host, String tagName, boolean shutdownSMs) {
        if (host.tags.remove(tagName) != null) {
            s_logger.info("Removing tag '" + tagName + "' from host " + host.address + " (id=" + host.id + ")");

            try {
                buildClient("/hosts/" + host.id + "/tags/" + urlEncodePathSegment(tagName)).delete();
            } catch (Exception e) {
                ApiException ape = ApiException.toApiException(e);
                if (ape.getErrorCode() != Status.NOT_FOUND) {
                    throw ape;
                }
            }
        }

        String dbName = dbConnInfo.getDbName();
        for (Process process : host.processes) {
            if (process.dbname.equals(dbName)) {
                if (shutdownSMs || !PROCESS_TYPE_SM.equals(process.type)) {
                    s_logger.info("Shutting down " + process.type + " process on host " + host.address + " (uid=" + process.uid + ")");
                    shutdownProcess(process.uid);
                }
            }
        }
    }

    protected HomeHostInfo findHomeHostInfo(Collection<Region> regions) {
        String homeRegionName = StorefrontApp.APP_INSTANCE.getRegion();
        String dbName = dbConnInfo.getDbName();
        Set<String> ipAddresses = NetworkUtil.getLocalIpAddresses();

        // Look for best match: Host running SM and sharing our IP and region
        HomeHostInfo smRegionMatch = null;
        HomeHostInfo ipRegionMatch = null;
        HomeHostInfo ipMatch = null;
        HomeHostInfo regionMatch = null;
        for (Region region : regions) {
            for (Host host : region.hosts) {
                HomeHostInfo match = new HomeHostInfo(host, region);

                if (homeRegionName.equals(region.region)) {
                    regionMatch = match;
                    for (Process process : host.processes) {
                        if (dbName.equals(process.dbname) && PROCESS_TYPE_SM.equals(process.type)) {
                            smRegionMatch = match;
                            break;
                        }
                    }
                    if (ipAddresses.contains(host.ipaddress)) {
                        ipRegionMatch = match;
                        if (smRegionMatch == ipRegionMatch) {
                            // Found best match
                            return smRegionMatch;
                        }
                    }
                } else if (ipAddresses.contains(host.ipaddress)) {
                    ipMatch = match;
                }
            }
        }

        // Second best match: Host running SM in our region
        if (smRegionMatch != null) {
            return smRegionMatch;
        }

        // Third best match: Host sharing our IP and region
        if (ipRegionMatch != null) {
            return ipRegionMatch;
        }

        // Fourth best match: Host sharing our region
        if (regionMatch != null) {
            return regionMatch;
        }

        // Fifth best match: Host sharing our IP
        if (ipMatch != null) {
            return ipMatch;
        }

        // Last resort: random host
        for (Region region : regions) {
            if (region.hosts.length > 0) {
                return new HomeHostInfo(region.hosts[0], region);
            }
        }

        // No host available
        return new HomeHostInfo();
    }

    protected Database findStorefrontDatabase(Collection<Region> regions) {
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

    protected DbFootprint getDbFootprint(Collection<Region> regions) {
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

    protected WebResource.Builder buildClient(String path) {
        String authHeader = "Basic " + new String(Base64.encode(apiConnInfo.getUsername() + ":" + apiConnInfo.getPassword()));
        return StorefrontFactory.createApiClient()
                .resource(apiConnInfo.getUrl() + path)
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .type(MediaType.APPLICATION_JSON);
    }

    @SuppressWarnings("unchecked")
    protected boolean fixDatabaseTemplate(Database database, int targetRegions, int targetHosts, HomeHostInfo homeHostInfo) {
        // Initialize DB tag constraint map (to specify host tags for SMs and TEs)
        Map<String, Map<String, String>> tagConstraints = new HashMap<String, Map<String, String>>();
        String dbProcessTag = dbConnInfo.getDbProcessTag();
        tagConstraints.put(DBVAR_TAG_CONSTRAINT_GROUP_SM, buildTagMustExistConstraint(dbProcessTag));
        tagConstraints.put(DBVAR_TAG_CONSTRAINT_GROUP_TE, buildTagMustExistConstraint(dbProcessTag));

        // Determine which template to use, and add template-specific variables
        Map<String, String> vars = new HashMap<String, String>();
        String templateName;
        if (targetRegions > 1) {
            templateName = TEMPLATE_GEO_DISTRIBUTED;
            vars.put(DBVAR_REGION, null);
            vars.put(DBVAR_HOST, null);
            vars.put(DBVAR_SM_MIN, "1");
            vars.put(DBVAR_SM_MAX, "2");
            vars.put(DBVAR_TE_MIN, "1");
        } else if (targetHosts > 1) {
            templateName = TEMPLATE_MULTI_HOST;
            vars.put(DBVAR_REGION, homeHostInfo.region.region);
            vars.put(DBVAR_HOST, null);
            vars.put(DBVAR_SM_MIN, "1");
            vars.put(DBVAR_SM_MAX, "2");
            vars.put(DBVAR_TE_MIN, "1");
        } else {
            templateName = TEMPLATE_SINGLE_HOST;
            vars.put(DBVAR_REGION, null);
            vars.put(DBVAR_HOST, homeHostInfo.host.id);
            vars.put(DBVAR_SM_MIN, null);
            vars.put(DBVAR_SM_MAX, null);
            vars.put(DBVAR_TE_MIN, null);
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
        database.template = templateName; // always set DB template to a string since any update request needs this as a string, not an object

        // Apply variables
        if (database.variables == null) {
            database.variables = new HashMap<String, String>();
        }
        if (database.tagConstraints == null) {
            database.tagConstraints = new HashMap<String, Map<String, String>>();
        }
        changeCount += applyVariables(database.variables, vars);
        changeCount += applyMapVariables(database.tagConstraints, tagConstraints);

        return changeCount > 0;
    }
    
    private static String urlEncodePathSegment(String str) {
        return UriComponent.encode(str, Type.PATH_SEGMENT);
    }

    private static Map<String, String> buildTagMustExistConstraint(String tagName) {
        Map<String, String> constraints = new HashMap<String, String>();
        constraints.put(tagName, DBVAR_TAG_EXISTS_CONSTRAINT);
        return constraints;
    }

    private static int applyVariables(Map<String, String> src, Map<String, String> vars) {
        int changeCount = 0;
        for (Map.Entry<String, String> varPair : vars.entrySet()) {
            if (varPair.getValue() == null) {
                if (src.remove(varPair.getKey()) != null) {
                    changeCount++;
                }
            } else {
                if (!varPair.getValue().equals(src.put(varPair.getKey(), varPair.getValue()))) {
                    changeCount++;
                }
            }
        }
        return changeCount;
    }

    private static int applyMapVariables(Map<String, Map<String, String>> src, Map<String, Map<String, String>> vars) {
        int changeCount = 0;
        for (Map.Entry<String, Map<String, String>> varPair : vars.entrySet()) {
            String key = varPair.getKey();
            Map<String, String> value = varPair.getValue();

            if (value == null) {
                if (src.remove(value) != null) {
                    changeCount++;
                }
            } else if (!src.containsKey(key)) {
                src.put(key, value);
                changeCount += value.size();
            } else {
                changeCount += applyVariables(src.get(key), value);
            }
        }
        return changeCount;
    }
}
