/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.dal;

import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.Callable;

import com.googlecode.genericdao.search.SearchResult;
import com.nuodb.storefront.model.dto.Category;
import com.nuodb.storefront.model.dto.ProductFilter;
import com.nuodb.storefront.model.dto.StorefrontStats;
import com.nuodb.storefront.model.dto.TransactionStats;
import com.nuodb.storefront.model.entity.IEntity;
import com.nuodb.storefront.model.entity.Product;
import com.nuodb.storefront.model.type.Currency;
import com.nuodb.storefront.service.IStorefrontService;

/**
 * General-purpose DAO with a few specialized methods to interact with the Storefront database. This interface and associated implementation(s) should
 * be used by Storefront services only.
 */
public interface IStorefrontDao extends IBaseDao {
    public void initialize(IEntity entity);

    /**
     * Evicts the model from a DAO session so that subsequent changes are not committed to the database.
     */
    public void evict(IEntity model);

    /**
     * @see IStorefrontService#getCategories()
     */
    public SearchResult<Category> getCategories();

    /**
     * @see IStorefrontService#getProducts(filter)
     */
    public SearchResult<Product> getProducts(ProductFilter filter);

    /**
     * Gets statistics on all transactions run by this service through either {@link #runTransaction(TransactionType, String, Callable)} or
     * {@link #runTransaction(TransactionType, String, Runnable)}. The keys of the returned map are transaction names as specified when these methods
     * are invoked.
     */
    public Map<String, TransactionStats> getTransactionStats();

    /**
     * Fetches stats for the storefront overall.
     * 
     * @param maxCustomerIdleTimeSec
     *            Max seconds a customer can be idle before being considered inactive.
     */
    public StorefrontStats getStorefrontStats(int maxCustomerIdleTimeSec);

    /**
     * Fetches stats for the storefront by region. Metrics that are not region-specific (like productCategoryCount) are placed in a region with an
     * empty string name, with 0 set in the other regions.
     * 
     * @param maxCustomerIdleTimeSec
     *            Max seconds a customer can be idle before being considered inactive.
     */
    public Map<String, StorefrontStats> getStorefrontStatsByRegion(int maxCustomerIdleTimeSec);

    /**
     * Removes instances from the AppInstances table who have not sent a heartbeat since the specified time.
     */
    public int deleteDeadAppInstances(Calendar maxLastHeartbeat);

    /**
     * Gets the "georegion" tag of the NuoDB transaction engine of the current database connection. Since the Storefront uses a thread pool and may
     * communicate with multiple transaction engines, the return value may vary if called multiple times. An exception is thrown if the underlying
     * database does not support georegion metadata (e.g. NuoDB pre-2.0, MySQL, etc.).
     */
    public String getCurrentDbNodeRegion();

    /**
     * Gets the currency currently associated with a specified region. This is determined by looking at the most recent Storefront instance (by last
     * heartbeat time) associated with this region. If no such instance exists, <code>null</code> is returned instead.
     */
    public Currency getRegionCurrency(String region);
}
