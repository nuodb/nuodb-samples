package com.nuodb.storefront.util;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;

public class PerformanceUtil {
    private static final OperatingSystemMXBean s_osInfo = ManagementFactory.getOperatingSystemMXBean();
    private static final Method s_getSystemCpuLoad = lookupNoArgMethod(s_osInfo.getClass(), "getSystemCpuLoad");

    private PerformanceUtil() {
    }

    private static Method lookupNoArgMethod(Class<?> clazz, String methodName) {
        try {
            Method method = clazz.getMethod(methodName, (Class<?>[]) null);
            method.setAccessible(true);
            return method;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @return number between 0 and 100 indicating current CPU utilization (%) across all processors.
     * 
     *         For systems not running Java 7 or later, always returns * 0.
     */
    public static int getCpuUtilization() {
        if (s_getSystemCpuLoad != null) {
            try {
                double cpuUtil = (Double) s_getSystemCpuLoad.invoke(s_osInfo, (Object[]) null);
                return (int) Math.round(cpuUtil * 100);
            } catch (Exception e) {
            }
        }
        return 0;
    }
}