package com.nuodb.storefront;


public class StorefrontApp {
    /**
     * Command line utility to perform various actions related to the Storefront
     * database. Specify each action as a separate argument. Valid actions are:
     * <ul>
     * <li>create -- create schema</li>
     * <li>drop -- drop schema</li>
     * <li>gen -- generate dummy storefront data</li>
     * </ul>
     */
    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String action = args[i];
            if ("create".equalsIgnoreCase(action)) {
                createSchema();
            } else if ("drop".equalsIgnoreCase(action)) {
                dropSchema();
            } else if ("gen".equalsIgnoreCase(action)) {
                generateData();
            } else {
                throw new IllegalArgumentException("Unknown action:  " + args[i]);
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
}
