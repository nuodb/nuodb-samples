package com.nuodb.storefront;

import java.io.PrintStream;
import java.util.Map;

import com.nuodb.storefront.model.WorkloadStats;
import com.nuodb.storefront.model.WorkloadStep;
import com.nuodb.storefront.model.WorkloadType;
import com.nuodb.storefront.service.ISimulatorService;

public class StorefrontApp {
    /**
     * Command line utility to perform various actions related to the Storefront application. Specify each action as a separate argument. Valid
     * actions are:
     * <ul>
     * <li>create -- create schema</li>
     * <li>drop -- drop schema</li>
     * <li>generate -- generate dummy storefront data</li>
     * <li>simulate -- simulate customer activity</li>
     * </ul>
     */
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            String action = args[i];
            if ("create".equalsIgnoreCase(action)) {
                createSchema();
            } else if ("drop".equalsIgnoreCase(action)) {
                dropSchema();
            } else if ("generate".equalsIgnoreCase(action)) {
                generateData();
            } else if ("simulate".equalsIgnoreCase(action)) {
                simulateActivity();
            } else {
                throw new IllegalArgumentException("Unknown action:  " + action);
            }
        }
    }

    public static void createSchema() {
        StorefrontFactory.createSchemaExport().create(true, true);
    }

    public static void dropSchema() {
        StorefrontFactory.createSchemaExport().drop(true, true);
    }

    public static void generateData() {
        StorefrontFactory.createDataGeneratorService().generate();
    }

    public static void simulateActivity() throws InterruptedException {
        ISimulatorService simulator = StorefrontFactory.createSimulatorService();
        simulator.addWorkload(WorkloadType.SIMILATED_BROWSER, 20, 250);
        simulator.addWorkload(WorkloadType.SIMILATED_SHOPPER_FAST, 20, 250);

        for (int i = 0; i < 20; i++) {
            printSimulatorStats(simulator, System.out);
            Thread.sleep(5 * 1000);
        }
        printSimulatorStats(simulator, System.out);
        simulator.shutdown();
    }

    private static void printSimulatorStats(ISimulatorService simulator, PrintStream out) {
        out.println();
        out.println(String.format("%-25s %8s %8s %8s %8s | %7s %9s %7s %9s", "Workload", "Active", "Failed", "Killed", "Complete", "Steps",
                "Avg (s)", "Work", "Avg (s)"));
        for (WorkloadStats stats : simulator.getWorkloadStats()) {
            out.println(String.format("%-25s %8d %8d %8d %8d | %7d %9.3f %7d %9.3f",
                    stats.getWorkloadType(),
                    stats.getActiveWorkerCount(),
                    stats.getFailedWorkerCount(),
                    stats.getKilledWorkerCount(),
                    stats.getCompletedWorkerCount(),
                    stats.getWorkInvocationCount(),
                    stats.getAvgWorkTimeMs() / 1000f,
                    stats.getWorkCompletionCount(),
                    stats.getAvgWorkCompletionTimeMs() / 1000f));
        }

        out.println();
        out.println(String.format("%-25s %20s", "Step:", "# Completions:"));
        for (Map.Entry<WorkloadStep, Integer> stepCount : simulator.getWorkloadStepCompletionCounts().entrySet()) {
            out.println(String.format("%-25s %20d", stepCount.getKey(), stepCount.getValue()));
        }
    }
}
