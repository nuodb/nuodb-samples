package com.nuodb.storefront.dal;

import org.hibernate.cfg.ImprovedNamingStrategy;

/**
 * Naming strategy similar to Hibernate's {@link ImprovedNamingStrategy}, except all names are in uppercase and foreign key columns include the name
 * of the referenced column for clarity.
 */
public class UpperCaseNamingStrategy extends ImprovedNamingStrategy {
    private static final long serialVersionUID = 1230229016645882001L;

    public UpperCaseNamingStrategy() {
    }

    @Override
    public String classToTableName(String className) {
        return super.classToTableName(className).toUpperCase();
    }

    @Override
    public String propertyToColumnName(String propertyName) {
        return super.propertyToColumnName(propertyName).toUpperCase();
    }

    @Override
    public String tableName(String tableName) {
        return super.tableName(tableName).toUpperCase();
    }

    @Override
    public String columnName(String columnName) {
        return super.columnName(columnName).toUpperCase();
    }

    @Override
    public String logicalColumnName(String columnName, String propertyName) {
        return super.logicalColumnName(columnName, propertyName).toUpperCase();
    }

    @Override
    public String logicalCollectionColumnName(String columnName, String propertyName, String referencedColumn) {
        return super.logicalCollectionColumnName(columnName, propertyName, referencedColumn).toUpperCase();
    }

    @Override
    public String logicalCollectionTableName(String tableName, String ownerEntityTable, String associatedEntityTable, String propertyName) {
        return super.logicalCollectionTableName(tableName, ownerEntityTable, associatedEntityTable, propertyName).toUpperCase();
    }

    @Override
    public String foreignKeyColumnName(String propertyName, String propertyEntityName, String propertyTableName, String referencedColumnName) {
        return super.foreignKeyColumnName(propertyName, propertyEntityName, propertyTableName, referencedColumnName) + "_"
                + referencedColumnName.toUpperCase();
    }
}
