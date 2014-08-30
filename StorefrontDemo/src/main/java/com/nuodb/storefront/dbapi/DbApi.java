/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.dbapi;

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
import com.nuodb.storefront.model.dto.DbStats;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.Base64;

public class DbApi implements IDbApi {
    private static final ClientConfig s_cfg = new DefaultClientConfig();
    private static final String TE_HOST_TAG = "TE_HOST_TAG";
    private static final String SM_HOST_TAG = "SM_HOST_TAG";
    private static final Logger s_logger = Logger.getLogger(DbApi.class.getName());

    private final String baseUrl;
    private final String authHeader;
    private final String apiUsername;
    private final DbConnInfo dbConnInfo;

    static {
        s_cfg.getSingletons().add(new JacksonJaxbJsonProvider().configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false));
    }

    public DbApi(String baseUrl, String apiUsername, String apiPassword, DbConnInfo dbConnInfo) {
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
        } catch (ClientHandlerException e) {
            throw new ApiUnavailableException(e);
        } catch (UniformInterfaceException e) {
            throw new ApiProxyException(e.getResponse(), e);
        } catch (Exception e) {
            throw new ApiProxyException(Status.INTERNAL_SERVER_ERROR, e.getMessage(), e);
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
        } catch (ClientHandlerException e) {
            throw new ApiUnavailableException(e);
        } catch (UniformInterfaceException e) {
            throw new ApiProxyException(e.getResponse(), e);
        } catch (Exception e) {
            throw new ApiProxyException(Status.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @Override
    public Database validateDbSetup() {
        Database db = validateDbSetup(getRegions());
        if (db == null) {
            throw new DatabaseNotFoundException();
        }

        // Ensure at least 1 node is running the DB
        DbStats stats = getDbStats(getRegions());
        if (stats.usedHostCount == 0) {
            setDbFootprint(1, 1);
        }

        return db;
    }

    @Override
    public Database fixDbSetup() {
        String dbName = dbConnInfo.getDbName();
        String dbTemplateName = dbConnInfo.getTemplate();
        String dbProcessTag = dbConnInfo.getDbProcessTag();
        for (Region region : getRegions()) {
            for (Database database : region.databases) {
                if (database.name.equals(dbName)) {
                    database.variables.put(SM_HOST_TAG, dbProcessTag);
                    database.variables.put(TE_HOST_TAG, dbProcessTag);
                    database.template = dbTemplateName;
                    return Client.create(s_cfg)
                            .resource(baseUrl + "/databases/" + database.name)
                            .header(HttpHeaders.AUTHORIZATION, authHeader)
                            .type(MediaType.APPLICATION_JSON)
                            .put(Database.class, database);
                }
            }
        }

        return createDatabase();
    }

    @Override
    public Database createDatabase() {
        try {
            // Ensure tag exists on just 1 host, 1 region
            setDbFootprint(1, 1);

            // Create DB
            Database db = new Database();
            db.name = dbConnInfo.getDbName();
            db.username = dbConnInfo.getUsername();
            db.password = dbConnInfo.getPassword();
            db.template = dbConnInfo.getTemplate();
            db.variables = new HashMap<String, String>();
            db.variables.put(TE_HOST_TAG, dbConnInfo.getDbProcessTag());
            db.variables.put(SM_HOST_TAG, dbConnInfo.getDbProcessTag());

            return Client.create(s_cfg)
                    .resource(baseUrl + "/databases/")
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .type(MediaType.APPLICATION_JSON)
                    .post(Database.class, db);
        } catch (ClientHandlerException e) {
            throw new ApiUnavailableException(e);
        } catch (UniformInterfaceException e) {
            throw new ApiProxyException(e.getResponse(), e);
        } catch (Exception e) {
            throw new ApiProxyException(Status.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @Override
    public List<Region> getRegions() {
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
        } catch (ClientHandlerException e) {
            throw new ApiUnavailableException(e);
        } catch (UniformInterfaceException e) {
            throw new ApiProxyException(e.getResponse(), e);
        } catch (Exception e) {
            throw new ApiProxyException(Status.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
    }

    @Override
    public DbStats getDbStats() {
        return getDbStats(getRegions());
    }

    @Override
    public synchronized DbStats setDbFootprint(int numRegions, int numHosts) {
        // Validate params
        if (numRegions < 1) {
            throw new DataValidationException("Number of regions must be positive");
        }
        if (numHosts < 1) {
            throw new DataValidationException("Number of regions must be positive");
        }

        List<Region> regions = getRegions();
        String dbProcessTag = dbConnInfo.getDbProcessTag();

        // Validate DB setup
        validateDbSetup(regions);

        // Determine which regions are currently used
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
        }

        // Ensure proper number of hosts are tagged in each used region
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
        }

        return getDbStats(regions);
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
                .resource(baseUrl + "/hosts/" + host.id + "/tags/" + tagName)
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

    protected Database validateDbSetup(List<Region> regions) {
        String dbName = dbConnInfo.getDbName();
        String dbTemplateName = dbConnInfo.getTemplate();
        String dbProcessTag = dbConnInfo.getDbProcessTag();
        for (Region region : regions) {
            for (Database database : region.databases) {
                if (database != null && database.name.equals(dbName)) {
                    @SuppressWarnings("rawtypes")
                    Map template = (Map) database.template;
                    if (!dbTemplateName.equals(template.get("name"))) {
                        throw new DataValidationException(
                                "Database '" + dbName + "' is configured with incorrect template '" + database.template
                                        + "':  '" + dbTemplateName + "' expected");
                    }
                    String smTag = database.variables.get(SM_HOST_TAG);
                    if (!dbProcessTag.equals(smTag)) {
                        throw new DataValidationException(
                                "Database '" + dbName + "' variable '" + SM_HOST_TAG + "' not set correctly:  '" + dbProcessTag + "' expected");
                    }
                    String teTag = database.variables.get(TE_HOST_TAG);
                    if (!dbProcessTag.equals(teTag)) {
                        throw new DataValidationException(
                                "Database '" + dbName + "' variable '" + TE_HOST_TAG + "' not set correctly:  '" + dbProcessTag + "' expected");

                    }
                    return database;
                }
            }
        }
        return null;
    }

    protected DbStats getDbStats(List<Region> regions) {
        DbStats dbStats = new DbStats();
        dbStats.regionCount = regions.size();

        for (Region region : regions) {
            dbStats.hostCount = Math.max(region.hostCount, dbStats.hostCount);
            dbStats.usedHostCount = Math.max(region.usedHostCount, dbStats.usedHostCount);
            if (region.usedHostCount > 0) {
                dbStats.usedRegionCount++;
            }
        }

        return dbStats;
    }
}
