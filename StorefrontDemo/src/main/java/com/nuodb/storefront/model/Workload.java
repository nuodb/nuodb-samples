package com.nuodb.storefront.model;

import java.util.ArrayList;
import java.util.List;

import com.nuodb.storefront.util.Randoms;

public class Workload implements Comparable<Workload> {
    public static final Workload BROWSER = new Workload("Browser", true, 20000, 10000, WorkloadStep.MULTI_BROWSE);
    public static final Workload SHOPPER = new Workload("Shopper", true, 20000, 10000, WorkloadStep.MULTI_SHOP);
    public static final Workload SHOPPER_FAST = new Workload("Fast shopper", true, 5000, 2500, WorkloadStep.MULTI_SHOP);
    public static final Workload REVIEWER = new Workload("Reviewer", true, 20000, 10000, WorkloadStep.MULTI_BROWSE_AND_REVIEW);

    private final String name;
    private final double avgThinkTimeMs;
    private final double thinkTimeVariance;
    private final boolean autoRepeat;
    private final WorkloadStep[] steps;
    private static final Randoms rnd = new Randoms();

    public Workload(String name) {
        this(name, false, 0, 0);
    }

    public Workload(String name, boolean autoRepeat, int avgThinkTimeMs, int thinkTimeStdDev, WorkloadStep... steps) {
        if (name == null) {
            throw new IllegalArgumentException("name");
        }
        
        this.name = name;
        this.autoRepeat = autoRepeat;
        this.avgThinkTimeMs = avgThinkTimeMs;
        this.thinkTimeVariance = Math.pow(thinkTimeStdDev, 2);

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

    public boolean isAutoRepeating() {
        return autoRepeat;
    }

    public long calcNextThinkTimeMs() {
        return Math.max(0L, Math.round(rnd.nextGaussian(avgThinkTimeMs, thinkTimeVariance)));
    }

    public String getName() {
        return name;
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

    @Override
    public int compareTo(Workload o) {
        return name.compareTo(o.name);
    }
    
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Workload && ((Workload)obj).name.equals(name);
    }
    
    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
