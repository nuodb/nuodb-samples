package com.nuodb.storefront.util;

import javax.servlet.http.HttpServletRequest;

public class RequestUtil {
    public static String getBaseHref(HttpServletRequest req) {
        String path = getRelativePath(req);
        String baseHref = "";
        for (int i = 1, n = path.length(); i < n; i++) {
            if (path.charAt(i) == '/') {
                baseHref += "../";
            }
        }
        return baseHref;
    }
    
    public static boolean isApiRequest(HttpServletRequest req) {
        return getRelativePath(req).startsWith("/api");
    }
    
    public static String getRelativePath(HttpServletRequest req) {
        return req.getRequestURI().substring(req.getContextPath().length());
    }
}
