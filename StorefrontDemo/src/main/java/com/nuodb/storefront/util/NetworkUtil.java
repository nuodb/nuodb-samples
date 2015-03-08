/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;

public class NetworkUtil {
    private NetworkUtil() {
    }

    public static String getLocalIpAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            return "localhost";
        }
    }

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
        return ipAddresses;
    }
}
