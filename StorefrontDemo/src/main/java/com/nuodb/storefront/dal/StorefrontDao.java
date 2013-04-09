package com.nuodb.storefront.dal;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl;
import com.googlecode.genericdao.search.SearchResult;
import com.nuodb.storefront.model.Category;
import com.nuodb.storefront.model.Model;
import com.nuodb.storefront.model.Product;
import com.nuodb.storefront.model.ProductFilter;
import com.nuodb.storefront.model.ProductSort;
import com.nuodb.storefront.model.StorefrontStats;
import com.nuodb.storefront.model.TransactionStats;

/**
 * Data access object designed for storefront operations, built on top of a general-purpose DAO. The caller is responsible for wrapping DAO calls in
 * transactions, typically by using the {@link #runTransaction(Callable)} or {@link #runTransaction(Runnable)} method.
 */
public class StorefrontDao extends GeneralDAOImpl implements IStorefrontDao {
    private static final long s_startTimeMs = System.currentTimeMillis();
    private static final String s_instanceId = UUID.randomUUID().toString();
    private static final String s_storefrontName = "Default Storefront";
    private static final Map<String, TransactionStats> s_transactionStatsMap = new HashMap<String, TransactionStats>();

    public StorefrontDao() {
    }

    /**
     * Registers a transaction name for transaction stats tracking. This is done for convenience to API clients, so they know up front all of the
     * transaction types available, even if some of those transaction types have not yet been executed and therefore have no stats associated with
     * them.
     */
    public static void registerTransactionNames(String[] transactionNames) {
        synchronized (s_transactionStatsMap) {
            for (String transactionName : transactionNames) {
                if (!s_transactionStatsMap.containsKey(transactionName)) {
                    s_transactionStatsMap.put(transactionName, new TransactionStats());
                }
            }
        }
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
    public void runTransaction(TransactionType transactionType, String name, final Runnable r) {
        runTransaction(transactionType, name, new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                r.run();
                return null;
            }
        });
    }

    @Override
    public <T> T runTransaction(TransactionType transactionType, String name, Callable<T> c) {
        long startTime = System.currentTimeMillis();

        Transaction t = this.getSession().beginTransaction();
        prepareSession(transactionType);
        try {
            T result = c.call();
            t.commit();
            updateTransactionStats(name, startTime, true);
            return result;
        } catch (Exception e) {
            t.rollback();
            updateTransactionStats(name, startTime, false);
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public SearchResult<Category> getCategories() {
        // This query got category usage counts, but was slower, and we currently don't need the counts for anything:
        // "select c, count(*) from Product p inner join p.categories c group by c order by c"

        List categories = getSession().createSQLQuery("select distinct category, 0 from product_category order by category").list();
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
    public SearchResult<Product> getProducts(ProductFilter filter) {
        Session session = getSession();

        SearchResult<Product> result = new SearchResult<Product>();
        result.setResult(buildProductQuery(filter, false).list());
        result.setTotalCount(((Number) buildProductQuery(filter, true).uniqueResult()).intValue());

        for (Product p : result.getResult()) {
            session.evict(p);
            p.clearReviews();
        }
        return result;
    }

    @Override
    public Map<String, TransactionStats> getTransactionStats() {
        Map<String, TransactionStats> mapCopy = new TreeMap<String, TransactionStats>();
        synchronized (s_transactionStatsMap) {
            for (Map.Entry<String, TransactionStats> entry : s_transactionStatsMap.entrySet()) {
                mapCopy.put(entry.getKey(), new TransactionStats(entry.getValue()));
            }
        }
        return mapCopy;
    }

    public StorefrontStats getStorefrontStats(int maxCustomerIdleTimeSec) {
        SQLQuery query = getSession().createSQLQuery("select"
                + " (select count(*) from product) as productCount,"
                + " (select count(*) from (select distinct category from product_category)) as categoryCount,"
                + " (select count(*) from product_review) as productReviewCount,"
                + " (select count(*) from customer) as customerCount,"
                + " (select count(*) from customer where datelastactive >= :minActiveTime) as activeCustomerCount,"
                + " (select count(*) from (select distinct customer_id from cart_selection)) as cartCount,"
                + " (select sum(quantity) from cart_selection) as cartItemCount,"
                + " (select sum(cast(quantity as decimal(8,2)) * unitprice) from cart_selection) as cartValue,"
                + " (select count(*) from purchase) as purchaseCount,"
                + " (select sum(quantity) from purchase_selection) as purchaseItemCount,"
                + " (select sum(cast(quantity as decimal(8,2)) * unitprice) from purchase_selection) as purchaseValue"
                + " from dual;");

        // Calc minActiveTime
        Calendar now = Calendar.getInstance();
        Calendar minActiveTime = (Calendar) now.clone();
        minActiveTime.add(Calendar.SECOND, -maxCustomerIdleTimeSec);
        query.setParameter("minActiveTime", minActiveTime);

        // Run query
        Object[] result = (Object[]) query.uniqueResult();

        // Fill stats
        StorefrontStats stats = new StorefrontStats();
        stats.setInstanceId(s_instanceId);
        stats.setTimestamp(now);
        stats.setStorefrontName(s_storefrontName);
        stats.setUptimeMs(System.currentTimeMillis() - s_startTimeMs);
        stats.setProductCount(Integer.valueOf(result[0].toString()));
        stats.setCategoryCount(Integer.valueOf(result[1].toString()));
        stats.setProductReviewCount(Integer.valueOf(result[2].toString()));
        stats.setCustomerCount(Integer.valueOf(result[3].toString()));
        stats.setActiveCustomerCount(Integer.valueOf(result[4].toString()));
        stats.setCartCount(Integer.valueOf(result[5].toString()));
        stats.setCartItemCount(Integer.valueOf(result[6].toString()));
        stats.setCartValue(new BigDecimal(result[7].toString()));
        stats.setPurchaseCount(Integer.valueOf(result[8].toString()));
        stats.setPurchaseItemCount(Integer.valueOf(result[9].toString()));
        stats.setPurchaseValue(new BigDecimal(result[10].toString()));

        return stats;
    }

    protected SQLQuery buildProductQuery(ProductFilter filter, boolean countOnly) {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<String, Object>();

        if (countOnly) {
            sql.append("select count(*) from product where 1=1");
        } else {
            sql.append("select * from product where 1=1");
        }

        // Set match text
        String matchText = filter.getMatchText();
        if (matchText != null && !matchText.isEmpty()) {
            matchText = "%" + matchText.trim().toLowerCase() + "%";
            sql.append(" and (lower(name) like :matchText or lower(description) like :matchText)");
            params.put("matchText", matchText);
        }

        // Set categories
        Collection<String> categories = filter.getCategories();
        if (categories != null && !categories.isEmpty()) {
            StringBuilder categoryParamList = new StringBuilder();
            int categoryIdx = 0;
            for (String category : categories) {
                if (categoryIdx > 0) {
                    categoryParamList.append(", ");
                }
                String catParamName = "cat" + ++categoryIdx;
                params.put(catParamName, category);
                categoryParamList.append(":" + catParamName);
            }
            sql.append(" and id in (select product_id from product_category where category in (" + categoryParamList + "))");
        }

        // Set sort
        ProductSort sort = filter.getSort();
        if (sort != null) {
            switch (sort) {
                case AVG_CUSTOMER_REVIEW:
                    sql.append(" order by coalesce(rating, -1) desc");
                    break;

                case DATE_CREATED:
                    sql.append(" order by dateadded desc");
                    break;

                case NEW_AND_POPULAR:
                    sql.append(" order by purchasecount desc, dateadded desc");
                    break;

                case PRICE_HIGH_TO_LOW:
                    sql.append(" order by unitprice desc");
                    break;

                case PRICE_LOW_TO_HIGH:
                    sql.append(" order by unitprice");
                    break;

                case RELEVANCE:
                    if (matchText != null && !matchText.isEmpty()) {
                        sql.append(" order by case when lower(name) like :matchText then 1 else 0 end desc, dateadded desc");
                    } else {
                        sql.append(" order by dateadded");
                    }
                    break;

                default:
                    sql.append(" order by id");
                    break;
            }
        }

        // Build SQL
        SQLQuery query = getSession().createSQLQuery(sql.toString());
        if (!countOnly) {
            query.addEntity(Product.class);
        }
        for (Map.Entry<String, Object> param : params.entrySet()) {
            query.setParameter(param.getKey(), param.getValue());
        }

        // Set pagination params (limit and offset)
        if (!countOnly) {
            Integer pageSize = filter.getPageSize();
            if (pageSize != null && pageSize > 0) {
                Integer page = filter.getPage();
                if (page != null) {
                    query.setFirstResult(pageSize * (page - 1));
                }
                query.setMaxResults(pageSize);
            }
        }

        return query;
    }

    protected void prepareSession(TransactionType transactionType) {
        switch (transactionType) {
            case READ_ONLY:
                Session session = getSession();

                // FIXME: Can't mark transaction as read-only with NuoDB right now, or SQL exceptions get thrown even with select statements
                // session.doWork(new Work() {
                // @Override
                // public void execute(Connection connection) throws SQLException {
                // connection.setReadOnly(true);
                // }
                // });

                session.setFlushMode(FlushMode.MANUAL);
                break;

            default:
                break;
        }
    }

    protected void updateTransactionStats(String transactionName, long startTimeMs, boolean success) {
        synchronized (s_transactionStatsMap) {
            TransactionStats stats = s_transactionStatsMap.get(transactionName);
            if (stats == null) {
                throw new RuntimeException("Transaction with name '" + transactionName + "' is not registered.");
            }
            stats.incrementCount(transactionName, System.currentTimeMillis() - startTimeMs, success);
        }
    }
}
