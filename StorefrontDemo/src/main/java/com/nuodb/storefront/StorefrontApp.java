/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import com.nuodb.storefront.model.dto.ProductFilter;
import com.nuodb.storefront.model.dto.Workload;
import com.nuodb.storefront.model.dto.WorkloadStats;
import com.nuodb.storefront.model.dto.WorkloadStep;
import com.nuodb.storefront.model.dto.WorkloadStepStats;
import com.nuodb.storefront.model.entity.Product;
import com.nuodb.storefront.service.IDataGeneratorService;
import com.nuodb.storefront.service.ISimulatorService;
import com.nuodb.storefront.service.IStorefrontService;
import com.nuodb.storefront.service.IStorefrontTenant;
import com.nuodb.storefront.util.DDLUtil;

public class StorefrontApp {
    static {
        // Try applying properties from specified file
        try {
            String propertyFile = System.getProperty("properties", null);
            if (propertyFile != null) {
                Properties overrides = new Properties();
                overrides.load(new FileInputStream(propertyFile));
                System.getProperties().putAll(overrides);
            }
        } catch (Exception e) {
            Logger.getLogger(StorefrontApp.class).warn("Failed to read properties file", e);
        }
        
        //  For JSP page compilation, use Jetty compiler when available to avoid JDK dependency
        System.setProperty("org.apache.jasper.compiler.disablejsr199", "true");        
    }
    
    // Storefront properties
    public static final String IP_DETECT_URL = System.getProperty("storefront.ipDetectUrl", "http://checkip.amazonaws.com");
    public static final String DEFAULT_URL = System.getProperty("storefront.url", "{protocol}://{host}:{port}/{context}");
    public static final int DEFAULT_PORT = Integer.valueOf(System.getProperty("maven.tomcat.port", "9001"));
    public static final String APP_NAME = System.getProperty("storefront.name", "NuoDB Storefront Demo");
    public static final String TENANT_PARAM_NAME = "tenant"; 
    public static final String LOGGER_NAME_TENANT_SEP = ":";
    
    // Storefront timings
    public static final int HEARTBEAT_INTERVAL_SEC = 10;
    public static final int CPU_SAMPLING_INTERVAL_SEC = 1;
    public static final int MAX_HEARTBEAT_AGE_SEC = 20;
    public static final int PURGE_FREQUENCY_SEC = 60 * 30; // 30 min
    public static final int STOP_USERS_AFTER_IDLE_UI_SEC = 60 * 10; // 10 min
    public static final int MIN_INSTANCE_PURGE_AGE_SEC = 60 * 60; // 1 hour
    public static final int DEFAULT_SESSION_TIMEOUT_SEC = 60 * 20;// 20 min
    public static final int DEFAULT_ANALYTIC_MAX_AGE = 60 * 30;// 30 min
    public static final int BENCHMARK_DURATION_MS = 10000;
    public static final int SIMULATOR_STATS_DISPLAY_INTERVAL_MS = 5000; 

    // Database properties
    public static final String DB_NAME = System.getProperty("storefront.db.name");
    public static final String DB_USER = System.getProperty("storefront.db.user");
    public static final String DB_PASSWORD = System.getProperty("storefront.db.password");
    public static final String DB_OPTIONS = System.getProperty("storefront.db.options");
    public static final int DB_PING_TIMEOUT_SEC = Integer.valueOf(System.getProperty("storefront.db.pingTimeoutSec", "0")); // 0=disabled
    public static final int DB_MAX_INIT_WAIT_TIME_SEC = Integer.valueOf(System.getProperty("storefront.db.initWaitTimeSec", "5"));
    public static final String DB_DOMAIN_BROKER =System.getProperty("domain.broker", "localhost");
    public static final String DB_PROCESS_TAG = System.getProperty("storefront.db.processTag", "demo_${db.name}");
    
    // Database API properties
    public static final int DBAPI_READ_TIMEOUT_SEC = Integer.valueOf(System.getProperty("storefront.dbapi.readTimeoutSec", "10"));
    public static final int DBAPI_CONNECT_TIMEOUT_SEC = Integer.valueOf(System.getProperty("storefront.dbapi.connectTimeoutSec", "10"));
    public static final int DBAPI_MAX_UNAVAILABLE_RETRY_TIME_SEC = Integer.valueOf(System.getProperty("storefront.dbapi.unavailableRetryTimeSec", "3"));
    public static final String DBAPI_HOST = System.getProperty("storefront.dbapi.host");
    public static final int DBAPI_PORT = Integer.valueOf(System.getProperty("storefront.dbapi.port", "8888"));
    public static final String DBAPI_USERNAME = System.getProperty("storefront.dbapi.user", "domain");
    public static final String DBAPI_PASSWORD = System.getProperty("storefront.dbapi.password", "bird");
    
    // Other properties
    public static final int SQLEXPLORER_PORT = Integer.valueOf(System.getProperty("storefront.sqlexplorer.port", "9001"));

    // Default names
    public static final String DEFAULT_DB_NAME = "Storefront";
    public static final String DEFAULT_DB_HOST = "localhost";
    public static final String DEFAULT_REGION_NAME = "Unknown region";
    public static final String DEFAULT_TENANT_NAME = "Default";
    
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
        IStorefrontTenant tenant = StorefrontTenantManager.getDefaultTenant();
        
        for (int i = 0; i < args.length; i++) {
            String action = args[i];
            if ("create".equalsIgnoreCase(action)) {
                createSchema(tenant.createSchemaExport());
                System.out.println("Tables created successfully.");
            } else if ("drop".equalsIgnoreCase(action)) {
                dropSchema(tenant.createSchemaExport());
                System.out.println("Tables dropped successfully.");
            } else if ("showddl".equalsIgnoreCase(action)) {
                showDdl(tenant.createSchemaExport());
            } else if ("generate".equalsIgnoreCase(action)) {
                System.out.println("Generating data...");
                generateData(tenant.createDataGeneratorService());
                System.out.println("Data generated successfully.  " + getProductStats(tenant.createStorefrontService()));
            } else if ("load".equalsIgnoreCase(action)) {
                System.out.println("Loading data...");
                loadData(tenant.createDataGeneratorService());
                System.out.println("Data loaded successfully.  " + getProductStats(tenant.createStorefrontService()));
            } else if ("simulate".equalsIgnoreCase(action)) {
                simulateActivity(tenant.getSimulatorService());
            } else if ("benchmark".equalsIgnoreCase(action)) {
                benchmark(tenant.getSimulatorService());
            } else {
                throw new IllegalArgumentException("Unknown action:  " + action);
            }
        }
    }

    protected static String getProductStats(IStorefrontService svc) {
        ProductFilter filter = new ProductFilter();
        filter.setPageSize(null);
        int numProducts = svc.getProducts(filter).getTotalCount();
        int numCategories = svc.getCategories().getTotalCount();
        return "There are now " + numProducts + " products across " + numCategories + " categories.";
    }

    public static void createSchema(SchemaExport export) {
        export.create(false, true);
    }

    public static void dropSchema(SchemaExport export) {
        export.drop(false, true);
    }

    public static void showDdl(SchemaExport export) {
        System.out.println(DDLUtil.generateAndFormat(export));
    }

    public static void generateData(IDataGeneratorService svc) throws IOException {
        try {
            svc.generateAll(100, 5000, 2, 10);
        } finally {
            svc.close();
        }
    }

    public static void removeData(IDataGeneratorService svc) throws IOException {
        try {
            svc.removeAll();
        } finally {
            svc.close();
        }
    }

    public static void loadData(IDataGeneratorService svc) throws IOException {
        InputStream stream = StorefrontApp.class.getClassLoader().getResourceAsStream("sample-products.json");
        ObjectMapper mapper = new ObjectMapper();

        // Read products from JSON file
        List<Product> products = mapper.readValue(stream, new TypeReference<ArrayList<Product>>() {
        });

        // Load products into DB, and load generated views
        try {
            svc.generateProductReviews(100, products, 10);
        } finally {
            svc.close();
        }
    }

    public static void benchmark(ISimulatorService simulator) throws InterruptedException {
        Workload shoppersWithNoWait = new Workload("Customer:  Instant purchaser", true, 0, 0, Workload.DEFAULT_MAX_WORKERS, WorkloadStep.MULTI_SHOP);

        simulator.addWorkers(shoppersWithNoWait, 100, 0);
        Thread.sleep(BENCHMARK_DURATION_MS);
        System.out.println(simulator.getWorkloadStats().get(shoppersWithNoWait.getName()).getWorkCompletionCount());
        simulator.removeAll();
    }

    public static void simulateActivity(ISimulatorService simulator) throws InterruptedException {
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
