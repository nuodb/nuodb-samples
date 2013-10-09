package com.nuodb.storefront.util;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;

public class PerformanceUtil {
    private static final OperatingSystemMXBean s_osInfo;
    private static final int s_availableProcessors;
    private static final Method s_getProcessCpuTime;

    private static long s_lastSystemTime;
    private static long s_lastProcessCpuTime;

    private PerformanceUtil() {
    }

    static {
        s_osInfo = ManagementFactory.getOperatingSystemMXBean();
        s_availableProcessors = s_osInfo.getAvailableProcessors();

        if (s_osInfo.getSystemLoadAverage() < 0) {
            Method getProcessCpuTime;
            try {
                getProcessCpuTime = s_osInfo.getClass().getMethod("getProcessCpuTime", (Class<?>[]) null);
                getProcessCpuTime.setAccessible(true);
                s_lastProcessCpuTime = (Long) getProcessCpuTime.invoke(s_osInfo, (Object[]) null);
                s_lastSystemTime = System.nanoTime();
            } catch (Exception e) {
                getProcessCpuTime = null;
            }
            s_getProcessCpuTime = getProcessCpuTime;
        } else {
            s_getProcessCpuTime = null; // not available or not needed
        }
    }

    /**
     * @return number between 0 and 100 indicating average CPU utilization (%) across all processors.
     */
    public static int getCpuUtilization() {
        double load;
        if (s_getProcessCpuTime != null) {
            synchronized (s_getProcessCpuTime) {
                long systemTime = System.nanoTime();
                long processCpuTime;
                try {
                    processCpuTime = (Long) s_getProcessCpuTime.invoke(s_osInfo, (Object[]) null);
                    load = (double) (processCpuTime - s_lastProcessCpuTime) / (systemTime - s_lastSystemTime);
                    s_lastProcessCpuTime = processCpuTime;
                    s_lastSystemTime = systemTime;
                } catch (Exception e) {
                    load = 0;
                }
            }
        } else {
            load = s_osInfo.getSystemLoadAverage();
        }

        if (load < 0) {
            return 0;
        }
        return (int) Math.round(load / s_availableProcessors * 100);
    }
}