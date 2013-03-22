package com.nuodb.storefront.dal;

import java.util.concurrent.Callable;

import com.googlecode.genericdao.dao.hibernate.GeneralDAO;
import com.googlecode.genericdao.search.SearchResult;
import com.nuodb.storefront.model.Category;
import com.nuodb.storefront.model.Model;
import com.nuodb.storefront.model.Product;
import com.nuodb.storefront.model.ProductFilter;
import com.nuodb.storefront.service.IStorefrontService;

/**
 * General-purpose DAO with a few specialized methods to interact with the
 * Storefront database. This interface and associated implementation(s) should
 * be used by Storefront services only.
 */
public interface IStorefrontDao extends GeneralDAO {
    public void initialize(Model model);
    
    /**
     * Evicts the model from a DAO session so that subsequent changes are not
     * committed to the database.
     */
    public void evict(Model model);

    /**
     * Invokes the {@link Runnable#run()} method within the context of a
     * transaction. Commits upon completion, or rolls back upon exception (and
     * then throws it).
     * 
     * @param r
     *            The instance to run.
     */
    public void runTransaction(TransactionType transactionType, Runnable r);

    /**
     * Invokes the {@link Callable#call()} method within the context of a
     * transaction. Commits upon completion (and returns call's value), or rolls
     * back upon exception (and then throws it).
     * 
     * @param r
     *            The instance to call.
     */
    public <T> T runTransaction(TransactionType transactionType, Callable<T> c);

    /**
     * @see IStorefrontService#getCategories()
     */
    public SearchResult<Category> getCategories();

    /**
     * @see IStorefrontService#getProducts(filter)
     */
    public SearchResult<Product> getProducts(ProductFilter filter);
}
