/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.nuodb.storefront.model.dto.ProductFilter;
import com.nuodb.storefront.model.dto.Workload;
import com.nuodb.storefront.model.dto.WorkloadStats;
import com.nuodb.storefront.model.dto.WorkloadStep;
import com.nuodb.storefront.model.dto.WorkloadStepStats;
import com.nuodb.storefront.model.entity.AppInstance;
import com.nuodb.storefront.model.entity.Product;
import com.nuodb.storefront.service.IDataGeneratorService;
import com.nuodb.storefront.service.ISimulatorService;
import com.nuodb.storefront.service.IStorefrontService;

public class StorefrontApp {
    public static final int HEARTBEAT_INTERVAL_SEC = 10;
    public static final int CPU_SAMPLING_INTERVAL_SEC = 1;
    public static final int MAX_HEARTBEAT_AGE_SEC = 20;
    public static final int PURGE_FREQUENCY_SEC = 60 * 30; // 30 min
    public static final int STOP_USERS_AFTER_IDLE_UI_SEC = 60 * 10; // 10 min
    public static final int MIN_INSTANCE_PURGE_AGE_SEC = 60 * 60; // 1 hour
    public static final int DEFAULT_SESSION_TIMEOUT_SEC = 60 * 20;// 20 min
    public static final int DEFAULT_ANALYTIC_MAX_AGE = 60 * 30;// 30 min
    public static final int MAX_DB_INIT_WAIT_TIME_SEC = 5;
    public static final int MAX_API_UNAVAILABLE_RETRY_TIME_SEC = 3;
    public static final int API_CONNECT_TIMEOUT_SEC = 10;
    public static final int API_READ_TIMEOUT_SEC = 10;

    public static final String DEFAULT_REGION_NAME = "Unknown region";
    public static final String DEFAULT_DB_NAME = "Storefront";
    public static final String DEFAULT_DB_HOST = "localhost";
    public static final String DEFAULT_DB_PROCESS_TAG_PREFIX = "Storefront DB=";
    public static final int DEFAULT_PORT = 8080;
    public static final String DEFAULT_URL = "{protocol}://{host}:{port}/{context}";

    public static final AppInstance APP_INSTANCE = new AppInstance(DEFAULT_REGION_NAME, true);

    private static final int BENCHMARK_DURATION_MS = 10000;
    private static final int SIMULATOR_STATS_DISPLAY_INTERVAL_MS = 5000;
    
    static {
        //  For JSP page compilation, use Jetty compiler when available to avoid JDK dependency
        System.setProperty("org.apache.jasper.compiler.disablejsr199", "true");
    }

    /**
     * Command line utility to perform various actions related to the Storefront application.
     * 
     * Specify each action as a separate argument. Valid actions are:
     * <ul>
     * <li>create -- create schema</li>
     * <li>drop -- drop schema</li>
     * <li>showddl -- display drop and create DDL</li>
     * <li>generate -- generate dummy storefront data</li>
     * <li>load -- load storefront data from src/main/resources/sample-products.json file</li>
     * <li>simulate -- simulate customer activity</li>
     * <li>benchmark -- run benchmark workload</li>
     * </ul>
     */
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            String action = args[i];
            if ("create".equalsIgnoreCase(action)) {
                createSchema();
                System.out.println("Tables created successfully.");
            } else if ("drop".equalsIgnoreCase(action)) {
                dropSchema();
                System.out.println("Tables dropped successfully.");
            } else if ("showddl".equalsIgnoreCase(action)) {
                showDdl();
            } else if ("generate".equalsIgnoreCase(action)) {
                System.out.println("Generating data...");
                generateData();
                System.out.println("Data generated successfully.  " + getProductStats());
            } else if ("load".equalsIgnoreCase(action)) {
                System.out.println("Loading data...");
                loadData();
                System.out.println("Data loaded successfully.  " + getProductStats());
            } else if ("simulate".equalsIgnoreCase(action)) {
                simulateActivity();
            } else if ("benchmark".equalsIgnoreCase(action)) {
                benchmark();
            } else {
                throw new IllegalArgumentException("Unknown action:  " + action);
            }
        }
    }

    protected static String getProductStats() {
        ProductFilter filter = new ProductFilter();
        filter.setPageSize(null);
        IStorefrontService svc = StorefrontFactory.createStorefrontService();
        int numProducts = svc.getProducts(filter).getTotalCount();
        int numCategories = svc.getCategories().getTotalCount();
        return "There are now " + numProducts + " products across " + numCategories + " categories.";
    }

    public static void createSchema() {
        StorefrontFactory.createSchemaExport().create(false, true);
    }

    public static void dropSchema() {
        StorefrontFactory.createSchemaExport().drop(false, true);
    }

    public static void showDdl() {
        StorefrontFactory.createSchemaExport().drop(true, false);
        StorefrontFactory.createSchemaExport().create(true, false);
    }

    public static void generateData() throws IOException {
        IDataGeneratorService svc = StorefrontFactory.createDataGeneratorService();
        try {
            svc.generateAll(100, 5000, 2, 10);
        } finally {
            svc.close();
        }
    }

    public static void removeData() throws IOException {
        IDataGeneratorService svc = StorefrontFactory.createDataGeneratorService();
        try {
            svc.removeAll();
        } finally {
            svc.close();
        }
    }

    public static void loadData() throws IOException {
        InputStream stream = StorefrontApp.class.getClassLoader().getResourceAsStream("sample-products.json");
        ObjectMapper mapper = new ObjectMapper();

        // Read products from JSON file
        List<Product> products = mapper.readValue(stream, new TypeReference<ArrayList<Product>>() {
        });

        // Load products into DB, and load generated views
        IDataGeneratorService svc = StorefrontFactory.createDataGeneratorService();
        try {
            svc.generateProductReviews(100, products, 10);
        } finally {
            svc.close();
        }
    }

    public static void benchmark() throws InterruptedException {
        Workload shoppersWithNoWait = new Workload("Customer:  Instant purchaser", true, 0, 0, Workload.DEFAULT_MAX_WORKERS, WorkloadStep.MULTI_SHOP);

        ISimulatorService simulator = StorefrontFactory.getSimulatorService();
        simulator.addWorkers(shoppersWithNoWait, 100, 0);
        Thread.sleep(BENCHMARK_DURATION_MS);
        System.out.println(simulator.getWorkloadStats().get(shoppersWithNoWait.getName()).getWorkCompletionCount());
        simulator.removeAll();
    }

    public static void simulateActivity() throws InterruptedException {
        ISimulatorService simulator = StorefrontFactory.getSimulatorService();
        simulator.adjustWorkers(Workload.BROWSER, 20, 25);
        simulator.addWorkers(Workload.BROWSER, 20, 250);
        simulator.addWorkers(Workload.SHOPPER_FAST, 20, 250);
        simulator.addWorkers(Workload.REVIEWER, 20, 250);

        for (int i = 0; i < 20; i++) {
            printSimulatorStats(simulator, System.out);
            Thread.sleep(SIMULATOR_STATS_DISPLAY_INTERVAL_MS);
        }
        printSimulatorStats(simulator, System.out);
        simulator.removeAll();
    }

    private static void printSimulatorStats(ISimulatorService simulator, PrintStream out) {
        out.println();
        out.println(String.format("%-30s %8s %8s %8s %8s | %7s %9s %7s %9s", "Workload", "Active", "Failed", "Killed", "Complete", "Steps",
                "Avg (s)", "Work", "Avg (s)"));
        for (Map.Entry<String, WorkloadStats> statsEntry : simulator.getWorkloadStats().entrySet()) {
            String workloadName = statsEntry.getKey();
            WorkloadStats stats = statsEntry.getValue();

            out.println(String.format("%-30s %8d %8d %8d %8d | %7d %9.3f %7d %9.3f",
                    workloadName,
                    stats.getActiveWorkerCount(),
                    stats.getFailedWorkerCount(),
                    stats.getKilledWorkerCount(),
                    stats.getCompletedWorkerCount(),
                    stats.getWorkInvocationCount(),
                    (stats.getAvgWorkTimeMs() != null) ? stats.getAvgWorkTimeMs() / 1000f : null,
                    stats.getWorkCompletionCount(),
                    (stats.getAvgWorkCompletionTimeMs() != null) ? stats.getAvgWorkCompletionTimeMs() / 1000f : null));
        }

        out.println();
        out.println(String.format("%-25s %20s", "Step:", "# Completions:"));
        for (Map.Entry<WorkloadStep, WorkloadStepStats> statsEntry : simulator.getWorkloadStepStats().entrySet()) {
            WorkloadStep step = statsEntry.getKey();
            WorkloadStepStats stats = statsEntry.getValue();
            out.println(String.format("%-25s %20d", step.name(), stats.getCompletionCount()));
        }
    }
}
