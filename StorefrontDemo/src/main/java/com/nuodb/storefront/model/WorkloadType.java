package com.nuodb.storefront.model;

import java.util.ArrayList;
import java.util.List;

import com.nuodb.storefront.util.Randoms;

public enum WorkloadType {
    REAL_USER,

    SIMULATED_USER_FACTORY,

    @WorkloadFlow(steps = { WorkloadStep.MULTI_BROWSE })
    SIMILATED_BROWSER(true, 20000, 10000),

    @WorkloadFlow(steps = { WorkloadStep.MULTI_SHOP })
    SIMILATED_SHOPPER(true, 20000, 10000),

    @WorkloadFlow(steps = { WorkloadStep.MULTI_BROWSE_AND_REVIEW })
    SIMILATED_REVIEWER(true, 20000, 10000),

    @WorkloadFlow(steps = { WorkloadStep.MULTI_SHOP })
    SIMILATED_SHOPPER_FAST(true, 5000, 2500),

    // not implemented yet
    SIMULATED_ADMIN_ANALYTICS;

    private final double avgThinkTimeMs;
    private final double thinkTimeVariance;
    private final boolean autoRepeat;
    private final WorkloadStep[] steps;
    private static final Randoms rnd = new Randoms();

    private WorkloadType() {
        this(false, 0, 0);
    }

    private WorkloadType(boolean autoRepeat, int avgThinkTimeMs, int thinkTimeStdDev) {
        this.autoRepeat = autoRepeat;
        this.avgThinkTimeMs = avgThinkTimeMs;
        this.thinkTimeVariance = Math.pow(thinkTimeStdDev, 2);

        try {
            List<WorkloadStep> steps = new ArrayList<WorkloadStep>();
            addSteps(this.getClass().getField(this.name()).getAnnotation(WorkloadFlow.class), steps);
            this.steps = steps.toArray(new WorkloadStep[steps.size()]);
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
        return Math.round(rnd.nextGaussian(avgThinkTimeMs, thinkTimeVariance));
    }

    private static void addSteps(WorkloadFlow annotation, List<WorkloadStep> accumulator) throws NoSuchFieldException {
        WorkloadStep[] steps = annotation.steps();
        for (int i = 0; i < steps.length; i++) {
            WorkloadStep step = steps[i];
            WorkloadFlow subStepsAnnotation = step.getClass().getField(step.name()).getAnnotation(WorkloadFlow.class);
            if (subStepsAnnotation != null && subStepsAnnotation.steps().length > 0) {
                addSteps(subStepsAnnotation, accumulator);
            } else {
                accumulator.add(step);
            }
        }
    }
}
