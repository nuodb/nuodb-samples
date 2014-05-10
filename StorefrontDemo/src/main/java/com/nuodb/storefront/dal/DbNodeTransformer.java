package com.nuodb.storefront.dal;

import java.util.List;

import org.hibernate.transform.ResultTransformer;

import com.nuodb.storefront.model.dto.DbNode;

public class DbNodeTransformer implements ResultTransformer {
    private static final long serialVersionUID = 211285415624172491L;

    public static final DbNodeTransformer INSTANCE = new DbNodeTransformer();

    private DbNodeTransformer() {
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List transformList(List collection) {
        return collection;
    }

    @Override
    public Object transformTuple(Object[] tuple, String[] aliases) {
        DbNode node = new DbNode();
        for (int i = 0; i < aliases.length; i++) {
            String alias = aliases[i].toLowerCase();
            if (alias.equals("id")) {
                node.setId((Integer) tuple[i]);
            } else if (alias.equals("localid")) {
                node.setLocalId((Integer) tuple[i]);
            } else if (alias.equals("port")) {
                node.setPort((Integer) tuple[i]);
            } else if (alias.equals("address")) {
                node.setAddress((String) tuple[i]);
            } else if (alias.equals("state")) {
                node.setState((String) tuple[i]);
            } else if (alias.equals("type")) {
                node.setType((String) tuple[i]);
            } else if (alias.equals("connstate")) {
                node.setConnState((String) tuple[i]);
            } else if (alias.equals("msgqsize")) {
                node.setMsgQSize((Integer) tuple[i]);
            } else if (alias.equals("triptime")) {
                node.setTripTime((Integer) tuple[i]);
            } else if (alias.equals("georegion")) {
                node.setGeoRegion((String) tuple[i]);
            } else if (alias.equals("local")) {
                node.setLocal(tuple[i].toString().equals("1"));
            }
        }
        return node;
    }
}
