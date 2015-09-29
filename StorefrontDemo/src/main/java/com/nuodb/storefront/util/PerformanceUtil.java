package com.nuodb.storefront.util;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;

public class PerformanceUtil {
    private static final OperatingSystemMXBean s_osInfo = ManagementFactory.getOperatingSystemMXBean();
    private static final Method s_getSystemCpuLoad = lookupNoArgMethod(s_osInfo.getClass(), "getSystemCpuLoad");
    private static Sampler s_sampler;
    private static final int NUM_SAMPLES = 30;

    private PerformanceUtil() {
    }
    
    public static long getGarbageCollectionTime() {
        long collectionTime = 0;
        for (GarbageCollectorMXBean garbageCollectorMXBean : ManagementFactory.getGarbageCollectorMXBeans()) {
            collectionTime += garbageCollectorMXBean.getCollectionTime();
        }
        return collectionTime;
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

    /**
     * @return Average CPU utilization over a sampling period, which is determined by the frequency with which the sampler is invoked and the number
     *         of samples retained ({@link #NUM_SAMPLES}).
     */
    public static int getAvgCpuUtilization() {
        if (s_sampler == null) {
            return getCpuUtilization();
        } else {
            return s_sampler.getAvgCpuUtilization();
        }
    }

    public static Runnable createSampler() {
        if (s_getSystemCpuLoad == null) {
            return null;
        }
        return s_sampler = new Sampler(NUM_SAMPLES);
    }

    private static class Sampler implements Runnable {
        private int[] samples;
        private int sampleIdx = 0;
        private float total;

        public Sampler(int numSamples) {
            if (numSamples <= 0) {
                throw new IllegalArgumentException();
            }
            samples = new int[numSamples];
            samples[0] = getCpuUtilization();
            total = samples[0];
        }

        public int getAvgCpuUtilization() {
            return (int) Math.round(total / samples.length);
        }

        @Override
        public void run() {
            total -= samples[sampleIdx];
            total += samples[sampleIdx++] = getCpuUtilization();
            if (sampleIdx >= samples.length) {
                sampleIdx = 0;
            }
        }
    }
}