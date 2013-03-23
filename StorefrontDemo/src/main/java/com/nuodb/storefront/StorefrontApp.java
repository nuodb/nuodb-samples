package com.nuodb.storefront;

import java.io.PrintStream;

import com.nuodb.storefront.model.WorkloadStats;
import com.nuodb.storefront.model.WorkloadType;
import com.nuodb.storefront.service.ISimulatorService;

public class StorefrontApp {
    /**
     * Command line utility to perform various actions related to the Storefront application. Specify each action as a separate argument. Valid
     * actions are:
     * <ul>
     * <li>create -- create schema</li>
     * <li>drop -- drop schema</li>
     * <li>datagen -- generate dummy storefront data</li>
     * <li>simulator -- simulate customer activity</li>
     * </ul>
     */
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            String action = args[i];
            if ("create".equalsIgnoreCase(action)) {
                createSchema();
            } else if ("drop".equalsIgnoreCase(action)) {
                dropSchema();
            } else if ("datagen".equalsIgnoreCase(action)) {
                generateData();
            } else if ("simulator".equalsIgnoreCase(action)) {
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
        simulator.addWorkload(WorkloadType.SIMILATED_BROWSER, 10, 500);
        simulator.addWorkload(WorkloadType.SIMILATED_SHOPPER_FAST, 10, 500);
        
        PrintStream out = System.out;
        for (int i = 0; i < 20; i++) {            
            Thread.sleep(5 * 1000);

            out.println();
            out.println(String.format("%10s %20s %20s %20s", "Workload:", "# Active users:", "# Actions:", "Avg time (ms):"));
            for (WorkloadStats stats : simulator.getWorkloadStats()) {
                out.println(String.format("%10s %20d %20d %20f", stats.getWorkloadType(), stats.getActiveUserCount(), stats.getTotalActionCount(), stats.getAvgActionTimeMs()));
            }
        }
        simulator.shutdown();
    }
}
