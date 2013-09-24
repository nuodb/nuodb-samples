package com.nuodb.storefront.util;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;

@SuppressWarnings("restriction")
public class PerformanceUtil {
    private static final OperatingSystemMXBean osInfo;
    private static final com.sun.management.OperatingSystemMXBean sunOsInfo;
    private static final int availableProcessors;

    private static long lastSystemTime;
    private static long lastProcessCpuTime;

    private PerformanceUtil() {
    }

    static {
        osInfo = ManagementFactory.getOperatingSystemMXBean();
        availableProcessors = osInfo.getAvailableProcessors();

        if (osInfo.getSystemLoadAverage() < 0 && osInfo instanceof com.sun.management.OperatingSystemMXBean) {
            sunOsInfo = (com.sun.management.OperatingSystemMXBean) osInfo;
            lastSystemTime = System.nanoTime();
            lastProcessCpuTime = sunOsInfo.getProcessCpuTime();
        } else {
            sunOsInfo = null; // not available or not needed
        }
    }

    /**
     * @return number between 0 and 100 indicating average CPU utilization (%) across all processors.
     */
    public static int getCpuUtilization() {
        double load;
        if (sunOsInfo != null) {
            synchronized (sunOsInfo) {
                long systemTime = System.nanoTime();
                long processCpuTime = sunOsInfo.getProcessCpuTime();
                load = (double) (processCpuTime - lastProcessCpuTime) / (systemTime - lastSystemTime);
                lastProcessCpuTime = processCpuTime;
                lastSystemTime = systemTime;
            }
        } else {
            load = osInfo.getSystemLoadAverage();
        }

        if (load < 0) {
            return 0;
        }
        return (int) Math.round(load / availableProcessors * 100);
    }
}