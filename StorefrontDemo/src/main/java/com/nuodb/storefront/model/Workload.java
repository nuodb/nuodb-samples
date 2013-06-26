/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.model;

import java.util.ArrayList;
import java.util.List;

import com.nuodb.storefront.util.Randoms;

public class Workload {
    private static final int DEFAULT_MAX_WORKERS = 50000;

    public static final Workload BROWSER = new Workload("Customer:  Browsing only", true, 20000, 10000, DEFAULT_MAX_WORKERS,
            WorkloadStep.MULTI_BROWSE);
    public static final Workload REVIEWER = new Workload("Customer:  Browsing & reviews", true, 20000, 10000, DEFAULT_MAX_WORKERS,
            WorkloadStep.MULTI_BROWSE_AND_REVIEW);
    public static final Workload SHOPPER = new Workload("Customer:  Slow purchaser", true, 20000, 10000, DEFAULT_MAX_WORKERS,
            WorkloadStep.MULTI_SHOP);
    public static final Workload SHOPPER_FAST = new Workload("Customer:  Fast purchaser", true, 5000, 2500, DEFAULT_MAX_WORKERS,
            WorkloadStep.MULTI_SHOP);
    public static final Workload SHOPPER_SUPER_FAST = new Workload("Customer:  Instant purchaser", true, 0, 0, DEFAULT_MAX_WORKERS,
            WorkloadStep.MULTI_SHOP);
    public static final Workload ANALYST = new Workload("Back office:  Analyst", true, 20000, 10000, DEFAULT_MAX_WORKERS,
            WorkloadStep.ADMIN_RUN_REPORT);

    private final String name;
    private double avgThinkTimeMs;
    private double thinkTimeVariance;
    private boolean autoRepeat;
    private final WorkloadStep[] steps;
    private final Randoms rnd = new Randoms();
    private int maxWorkers;

    public Workload(String name) {
        this(name, false, 0, 0, DEFAULT_MAX_WORKERS);
    }

    public Workload(String name, boolean autoRepeat, int avgThinkTimeMs, int thinkTimeStdDev, int maxWorkers, WorkloadStep... steps) {
        if (name == null) {
            throw new IllegalArgumentException("name");
        }

        this.name = name;
        this.autoRepeat = autoRepeat;
        this.avgThinkTimeMs = avgThinkTimeMs;
        this.thinkTimeVariance = Math.pow(thinkTimeStdDev, 2);
        this.maxWorkers = maxWorkers;

        try {
            List<WorkloadStep> expandedSteps = new ArrayList<WorkloadStep>();
            expandSteps(steps, expandedSteps);
            this.steps = expandedSteps.toArray(new WorkloadStep[expandedSteps.size()]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public WorkloadStep[] getSteps() {
        return steps;
    }

    public long calcNextThinkTimeMs() {
        return Math.max(0L, Math.round(rnd.nextGaussian(avgThinkTimeMs, thinkTimeVariance)));
    }

    public String getName() {
        return name;
    }

    public double getAvgThinkTimeMs() {
        return avgThinkTimeMs;
    }

    public void setAvgThinkTimeMs(double avgThinkTimeMs) {
        this.avgThinkTimeMs = avgThinkTimeMs;
    }

    public double getThinkTimeVariance() {
        return thinkTimeVariance;
    }

    public void setThinkTimeVariance(double thinkTimeVariance) {
        this.thinkTimeVariance = thinkTimeVariance;
    }

    public boolean isAutoRepeat() {
        return autoRepeat;
    }

    public void setAutoRepeat(boolean autoRepeat) {
        this.autoRepeat = autoRepeat;
    }
    
    public int getMaxWorkers() {
        return maxWorkers;
    }

    public void setMaxWorkers(int maxWorkers) {
        this.maxWorkers = maxWorkers;
    }

    private static void expandSteps(WorkloadStep[] steps, List<WorkloadStep> accumulator) throws NoSuchFieldException {
        if (steps != null) {
            for (int i = 0; i < steps.length; i++) {
                WorkloadStep step = steps[i];
                WorkloadFlow subStepsAnnotation = step.getClass().getField(step.name()).getAnnotation(WorkloadFlow.class);
                if (subStepsAnnotation != null) {
                    expandSteps(subStepsAnnotation.steps(), accumulator);
                } else {
                    accumulator.add(step);
                }
            }
        }
    }
}
