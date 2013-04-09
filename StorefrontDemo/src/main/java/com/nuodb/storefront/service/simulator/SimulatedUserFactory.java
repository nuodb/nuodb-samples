package com.nuodb.storefront.service.simulator;

import com.nuodb.storefront.model.Workload;

public class SimulatedUserFactory implements IWorker {
    private static final Workload factoryWorkloadType = new Workload("User factory");
    private final ISimulator simulator;
    private final Workload workerWorkloadType;
    private final long entryDelayMs;
    private int userCount;

    public SimulatedUserFactory(ISimulator simulator, Workload workerWorkloadType, int userCount, long entryDelayMs) {
        if (simulator == null) {
            throw new IllegalArgumentException("simulator");
        }
        if (workerWorkloadType.getSteps() == null || workerWorkloadType.getSteps().length == 0) {
            throw new IllegalArgumentException("workerWorkloadType has no steps associated with it");
        }
        if (userCount < 0) {
            throw new IllegalArgumentException("userCount");
        }
        if (entryDelayMs < 0) {
            throw new IllegalArgumentException("entryDelayMs");
        }

        this.simulator = simulator;
        this.workerWorkloadType = workerWorkloadType;
        this.userCount = userCount;
        this.entryDelayMs = entryDelayMs;
    }

    @Override
    public Workload getWorkload() {
        return factoryWorkloadType;
    }

    @Override
    public long doWork() {
        if (userCount > 0) {
            do {
                simulator.addWorker(new SimulatedUser(simulator, workerWorkloadType), 0);
            } while (--userCount > 0 && entryDelayMs == 0);
        }

        if (userCount <= 0) {
            return IWorker.COMPLETE_NO_REPEAT;
        }

        return entryDelayMs;
    }
}
