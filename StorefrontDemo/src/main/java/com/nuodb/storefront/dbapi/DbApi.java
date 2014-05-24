/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.dbapi;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.DeserializationConfig;

import com.nuodb.storefront.exception.ApiProxyException;
import com.nuodb.storefront.model.dto.DbNode;
import com.nuodb.storefront.util.StringUtils;
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
    public List<DbNode> getDbNodes(String dbName) {
        try {
            List<DbNode> nodes = new ArrayList<DbNode>();

            Region[] regions = Client.create(s_cfg)
                    .resource(baseUrl + "/api/regions")
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .type(MediaType.APPLICATION_JSON)
                    .get(Region[].class);

            for (int regionIdx = 0; regionIdx < regions.length; regionIdx++) {
                Region region = regions[regionIdx];
                for (int databaseIdx = 0; databaseIdx < region.databases.length; databaseIdx++) {
                    Database database = region.databases[databaseIdx];
                    if (dbName == null || dbName.equals(database.name)) {
                        for (int processIdx = 0; processIdx < database.processes.length; processIdx++) {
                            Process process = database.processes[processIdx];
                            DbNode dbNode = new DbNode();
                            dbNode.setAddress(process.address);
                            dbNode.setAgentId(process.agentid);
                            dbNode.setConnState(null);
                            dbNode.setGeoRegion(region.region);
                            dbNode.setId(process.nodeId);
                            dbNode.setLocal(false);
                            dbNode.setLocalId(null);
                            dbNode.setMsgQSize(null);
                            dbNode.setPid(process.pid);
                            dbNode.setPort(process.port);
                            dbNode.setState(StringUtils.toSentenceCase(process.status));
                            dbNode.setTripTime(null);
                            dbNode.setType(process.type);
                            dbNode.setUid(process.uid);
                            nodes.add(dbNode);
                        }
                    }
                }
            }

            return nodes;
        } catch (UniformInterfaceException e) {
            throw new ApiProxyException(Status.fromStatusCode(e.getResponse().getStatus()));
        } catch (Exception e) {
            throw new ApiProxyException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public void shutdownDbNode(String uid) {
        try {
            Client.create(s_cfg)
                    .resource(baseUrl + "/api/processes/" + uid)
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .type(MediaType.APPLICATION_JSON)
                    .delete();
        } catch (UniformInterfaceException e) {
            throw new ApiProxyException(Status.fromStatusCode(e.getResponse().getStatus()));
        } catch (Exception e) {
            throw new ApiProxyException(Status.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
