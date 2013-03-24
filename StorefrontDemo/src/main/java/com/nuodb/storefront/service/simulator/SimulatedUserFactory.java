package com.nuodb.storefront.service.simulator;

import com.nuodb.storefront.model.WorkloadType;

public class SimulatedUserFactory implements IWorker {
    private final ISimulator simulator;
    private final WorkloadType workloadType;
    private final int entryDelayMs;
    private int userCount;

    public SimulatedUserFactory(ISimulator simulator, WorkloadType workloadType, int userCount, int entryDelayMs) {
        if (simulator == null) {
            throw new IllegalArgumentException("simulator"); 
        }
        if (workloadType.getSteps() == null || workloadType.getSteps().length == 0) {
            throw new IllegalArgumentException("workerType has no steps associated with it");
        }
        if (userCount < 0) {
            throw new IllegalArgumentException("userCount");
        }
        if (entryDelayMs < 0) {
            throw new IllegalArgumentException("entryDelayMs");
        }

        this.simulator = simulator;
        this.workloadType = workloadType;
        this.userCount = userCount;
        this.entryDelayMs = entryDelayMs;
    }

    @Override
    public WorkloadType getWorkloadType() {
        return WorkloadType.SIMULATED_USER_FACTORY;
    }

    @Override
    public long doWork() {
        if (userCount > 0) {
            do {
                simulator.addWorker(new SimulatedUser(simulator, workloadType), 0);
            } while (--userCount > 0 && entryDelayMs == 0);
        }

        if (userCount <= 0) {
            return IWorker.COMPLETE_NO_REPEAT;
        }
        
        return entryDelayMs;
    }
}
