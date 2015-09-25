/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import com.nuodb.storefront.StorefrontApp;

public class NetworkUtil {
    private NetworkUtil() {
    }

    /**
     * Provides the the best guest of the IP address of this machine, with a preference to an external IP address, if available. If none could not be
     * detected, "localhost" is returned.
     */
    public static String getLocalIpAddress() {
        String externalIp = detectExternalIpAddress();
        if (!StringUtils.isEmpty(externalIp)) {
            return externalIp;
        }

        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }

    /**
     * Provides a set of all of the IP addresses associated with this machine, including an external one (if available).
     */
    public static Set<String> getLocalIpAddresses() {
        Set<String> ipAddresses = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        try {
            Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements())
            {
                Enumeration<InetAddress> ee = e.nextElement().getInetAddresses();
                while (ee.hasMoreElements())
                {
                    ipAddresses.add(ee.nextElement().getHostAddress());
                }
            }
        } catch (SocketException e) {
        }
        String externalIp = detectExternalIpAddress();
        if (!StringUtils.isEmpty(externalIp)) {
            ipAddresses.add(externalIp);
        }
        return ipAddresses;
    }

    private static String detectExternalIpAddress() {
        if (StringUtils.isEmpty(StorefrontApp.IP_DETECT_URL)) {
            return null;
        }

        try {
            URL whatismyip = new URL(StorefrontApp.IP_DETECT_URL);
            BufferedReader in = new BufferedReader(new InputStreamReader(whatismyip.openStream()));
            String ip = in.readLine();
            InetAddress.getByName(ip);
            in.close();
            return ip;
        } catch (IOException e) {
            return null;
        }
    }
}
