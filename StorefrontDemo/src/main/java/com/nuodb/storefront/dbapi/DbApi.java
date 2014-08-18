/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.dbapi;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.DeserializationConfig;

import com.nuodb.storefront.exception.ApiProxyException;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.core.util.Base64;

public class DbApi implements IDbApi {
    private static final ClientConfig s_cfg = new DefaultClientConfig();
    private String baseUrl;
    private String authHeader;

    static {
        s_cfg.getSingletons().add(new JacksonJaxbJsonProvider().configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false));
    }

    public DbApi(String baseUrl, String apiUsername, String apiPassword) {
        this.baseUrl = baseUrl;
        this.authHeader = "Basic " + new String(Base64.encode(apiUsername + ":" + apiPassword));
    }

    @Override
    public List<Process> getProcesses(String dbName) {
        try {
            List<Process> nodes = new ArrayList<Process>();
            List<Region> regions = getRegions();

            for (Region region : regions) {
                for (int databaseIdx = 0; databaseIdx < region.databases.length; databaseIdx++) {
                    Database database = region.databases[databaseIdx];
                    if (dbName == null || dbName.equals(database.name)) {
                        for (int processIdx = 0; processIdx < database.processes.length; processIdx++) {
                            Process process = database.processes[processIdx];
                            process.region = region.region;
                            nodes.add(process);
                        }
                    }
                }
            }

            return nodes;
        } catch (UniformInterfaceException e) {
            throw new ApiProxyException(e.getResponse());
        } catch (Exception e) {
            throw new ApiProxyException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public void shutdownProcess(String uid) {
        try {
            Client.create(s_cfg)
                    .resource(baseUrl + "/api/processes/" + uid)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .type(MediaType.APPLICATION_JSON)
                    .delete();
        } catch (UniformInterfaceException e) {
            throw new ApiProxyException(e.getResponse());
        } catch (Exception e) {
            throw new ApiProxyException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public Database createDatabase(String dbName, String username, String password, String template) {
        try {
            Database db = new Database();
            db.name = dbName;
            db.username = username;
            db.password = password;
            db.template = template;

            return Client.create(s_cfg)
                    .resource(baseUrl + "/api/databases/")
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .type(MediaType.APPLICATION_JSON)
                    .post(Database.class, db);
        } catch (UniformInterfaceException e) {
            throw new ApiProxyException(e.getResponse());
        } catch (Exception e) {
            throw new ApiProxyException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public List<Region> getRegions() {
        try {
            Region[] regions = Client.create(s_cfg)
                    .resource(baseUrl + "/api/regions")
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .type(MediaType.APPLICATION_JSON)
                    .get(Region[].class);
            return Arrays.asList(regions);
        } catch (UniformInterfaceException e) {
            throw new ApiProxyException(e.getResponse());
        } catch (Exception e) {
            throw new ApiProxyException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
