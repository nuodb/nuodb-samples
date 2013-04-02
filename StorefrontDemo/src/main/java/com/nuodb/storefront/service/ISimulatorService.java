package com.nuodb.storefront.service;

import java.util.Map;

import com.nuodb.storefront.model.Workload;
import com.nuodb.storefront.model.WorkloadStats;
import com.nuodb.storefront.model.WorkloadStep;
import com.nuodb.storefront.model.WorkloadStepStats;

public interface ISimulatorService {
    public void addWorkload(Workload workload, int numWorkers, int entryDelayMs);
    
    public void downsizeWorkload(Workload workload, int newWorkerLimit);
    
    public void removeAll();
    
    public Map<Workload, WorkloadStats> getWorkloadStats();
    
    public Map<WorkloadStep, WorkloadStepStats> getWorkloadStepStats();
    
    /**
     * Blocking call to stop all simulator threads, running work, and queued work.
     * Once this method is called, you cannot add additional workloads.
     */
    public void shutdown();
}
