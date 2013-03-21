package com.nuodb.storefront.dal;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;

import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl;
import com.googlecode.genericdao.search.Filter;
import com.googlecode.genericdao.search.Search;
import com.googlecode.genericdao.search.SearchResult;
import com.nuodb.storefront.model.Category;
import com.nuodb.storefront.model.Model;
import com.nuodb.storefront.model.Product;
import com.nuodb.storefront.service.ProductSort;

/**
 * Data access object designed for storefront operations, built on top of a
 * general-purpose DAO. The caller is responsible for wrapping DAO calls in
 * transactions, typically by using the {@link #runTransaction(Callable)} or
 * {@link #runTransaction(Runnable)} method.
 */
public class StorefrontDao extends GeneralDAOImpl implements IStorefrontDao {
    public StorefrontDao() {
    }

    @Override
    public void initialize(Model model) {
        Hibernate.initialize(model);
    }

    @Override
    public void evict(Model model) {
        getSession().evict(model);
    }

    @Override
    public void runTransaction(TransactionType transactionType, Runnable r) {
        Transaction t = this.getSession().beginTransaction();
        prepareSession(transactionType);
        try {
            r.run();
            t.commit();
        } catch (RuntimeException e) {
            t.rollback();
            throw e;
        }
    }

    @Override
    public <T> T runTransaction(TransactionType transactionType, Callable<T> c) {
        Transaction t = this.getSession().beginTransaction();
        prepareSession(transactionType);
        try {
            T result = c.call();
            t.commit();
            return result;
        } catch (Exception e) {
            t.rollback();
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public SearchResult<Category> getCategories() {
        List categories = getSession().createQuery("select c, count(*) from Product p inner join p.categories c group by c order by c").list();
        for (int i = categories.size() - 1; i >= 0; i--) {
            Object[] data = (Object[]) categories.get(i);
            categories.set(i, new Category((String) data[0], ((Number) data[1]).intValue()));
        }

        SearchResult result = new SearchResult<Category>();
        result.setResult(categories);
        result.setTotalCount(categories.size());
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public SearchResult<Product> getProducts(String matchText, Collection<String> categories, Integer page, Integer pageSize, ProductSort sort) {
        /*
         * if (sort == ProductSort.AVG_CUSTOMER_REVIEW) { String sql =
         * "select product_id from product_review group by product_id order by avg(cast(rating as float)) desc"
         * ; if (pageSize != null) { if (page != null) { sql += " offset " +
         * (page * pageSize); } sql += " fetch " + pageSize; }
         * getSession().createSQLQuery(sql).list(); }
         */

        final Search search = new Search(Product.class);

        if (matchText != null && !matchText.isEmpty()) {
            matchText = "%" + matchText.trim() + "%";
            search.addFilterOr(Filter.ilike("name", matchText), Filter.ilike("description", matchText));
        }
        if (categories != null && !categories.isEmpty()) {
            search.addFilterSome("categories", Filter.in(Filter.ROOT_ENTITY, categories));
        }
        if (page != null) {
            search.setPage(page - 1);
        }
        if (pageSize != null) {
            search.setMaxResults(pageSize);
        }
        if (sort != null) {
            switch (sort) {
                case AVG_CUSTOMER_REVIEW:
                    // TODO
                    // search.addSortDesc("rating");
                    break;

                case DATE_CREATED:
                    search.addSortDesc("dateAdded");
                    break;

                case NEW_AND_POPULAR:
                    // TODO
                    // search.addSortDesc("rating");
                    search.addSortDesc("dateAdded");
                    break;

                case PRICE_HIGH_TO_LOW:
                    search.addSortDesc("unitPrice");
                    break;

                case PRICE_LOW_TO_HIGH:
                    search.addSortAsc("unitPrice");
                    break;

                case RELEVANCE:
                    // TODO
                    break;

                default:
                    break;
            }
        }

        Session session = getSession();
        SearchResult<Product> result = searchAndCount(search);
        for (Product p : result.getResult()) {
            session.evict(p);
            p.clearReviews();
        }
        return result;
    }

    protected void prepareSession(TransactionType transactionType) {
        switch (transactionType) {
            case READ_ONLY:
                Session session = getSession();
                
                // FIXME:  Can't mark transaction as read-only with NuoDB right now, or SQL exceptions get thrown even with select statements 
//                session.doWork(new Work() {
//                    @Override
//                    public void execute(Connection connection) throws SQLException {
//                        connection.setReadOnly(true);
//                    }
//                });
                
                session.setFlushMode(FlushMode.MANUAL);
                break;

            default:
                break;
        }
    }
}
