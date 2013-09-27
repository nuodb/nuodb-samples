/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.dal;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import org.hibernate.FlushMode;
import org.hibernate.Hibernate;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.transform.ResultTransformer;

import com.googlecode.genericdao.dao.hibernate.GeneralDAOImpl;
import com.googlecode.genericdao.search.SearchResult;
import com.nuodb.storefront.model.dto.Category;
import com.nuodb.storefront.model.dto.DbNode;
import com.nuodb.storefront.model.dto.ProductFilter;
import com.nuodb.storefront.model.dto.StorefrontStats;
import com.nuodb.storefront.model.dto.TransactionStats;
import com.nuodb.storefront.model.entity.IEntity;
import com.nuodb.storefront.model.entity.Product;
import com.nuodb.storefront.model.type.ProductSort;
import com.nuodb.storefront.service.storefront.HeartbeatService;

/**
 * Data access object designed for storefront operations, built on top of a general-purpose DAO. The caller is responsible for wrapping DAO calls in
 * transactions, typically by using the {@link #runTransaction(Callable)} or {@link #runTransaction(Runnable)} method.
 */
public class StorefrontDao extends GeneralDAOImpl implements IStorefrontDao {
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
    public void initialize(IEntity entity) {
        Hibernate.initialize(entity);
    }

    @Override
    public void evict(IEntity entity) {
        getSession().evict(entity);
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

        List categories = getSession().createSQLQuery("SELECT DISTINCT CATEGORY, 0 FROM PRODUCT_CATEGORY ORDER BY CATEGORY").list();
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
        // Run query
        SQLQuery query = getSession().createSQLQuery("SELECT"
                + " (SELECT COUNT(*) FROM PRODUCT) AS PRODUCT_COUNT,"
                + " (SELECT COUNT(*) FROM (SELECT DISTINCT CATEGORY FROM PRODUCT_CATEGORY) AS A) AS CATEGORY_COUNT,"
                + " (SELECT COUNT(*) FROM PRODUCT_REVIEW) AS PRODUCT_REVIEW_COUNT,"
                + " (SELECT COUNT(*) FROM CUSTOMER) AS CUSTOMER_COUNT,"
                + " (SELECT COUNT(*) FROM CUSTOMER WHERE DATE_LAST_ACTIVE >= :MIN_ACTIVE_TIME) AS ACTIVE_CUSTOMER_COUNT,"
                + " (SELECT COUNT(*) FROM CUSTOMER WHERE WORKLOAD IS NULL AND DATE_LAST_ACTIVE >= :MIN_ACTIVE_TIME) AS ACTIVE_WEB_CUSTOMER_COUNT,"
                + " (SELECT SUM(QUANTITY) FROM CART_SELECTION) AS CART_ITEM_COUNT,"
                + " (SELECT SUM(CAST(QUANTITY AS DECIMAL(16,2)) * UNIT_PRICE) FROM CART_SELECTION) AS CART_VALUE,"
                + " (SELECT COUNT(*) FROM PURCHASE) AS PURCHASE_COUNT,"
                + " (SELECT SUM(QUANTITY) FROM PURCHASE_SELECTION) AS PURCHASE_ITEM_COUNT,"
                + " (SELECT SUM(CAST(QUANTITY AS DECIMAL(16,2)) * UNIT_PRICE) FROM PURCHASE_SELECTION) AS PURCHASE_VALUE,"
                + " (SELECT MIN(DATE_STARTED) FROM APP_INSTANCE WHERE LAST_HEARTBEAT >= :MIN_HEARTBEAT_TIME) AS START_TIME"
                + " FROM DUAL;");
        setStatsParameters(query, maxCustomerIdleTimeSec);
        Object[] result = (Object[]) query.uniqueResult();

        // Fill stats
        StorefrontStats stats = new StorefrontStats();
        stats.setProductCount(getIntValue(result[0]));
        stats.setCategoryCount(getIntValue(result[1]));
        stats.setProductReviewCount(getIntValue(result[2]));
        stats.setCustomerCount(getIntValue(result[3]));
        stats.setActiveCustomerCount(getIntValue(result[4]));
        stats.setActiveWebCustomerCount(getIntValue(result[5]));
        stats.setCartItemCount(getIntValue(result[6]));
        stats.setCartValue(getBigDecimalValue(result[7]));
        stats.setPurchaseCount(getIntValue(result[8]));
        stats.setPurchaseItemCount(getIntValue(result[9]));
        stats.setPurchaseValue(getBigDecimalValue(result[10]));

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(getLongValue(result[11]));
        stats.setDateStarted(cal);

        return stats;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, StorefrontStats> getStorefrontStatsByRegion(int maxCustomerIdleTimeSec) {
        Map<String, StorefrontStats> regionStatsMap = new TreeMap<String, StorefrontStats>();

        // Run query
        SQLQuery query = getSession()
                .createSQLQuery(
                        " SELECT 'productCount' AS METRIC_NAME, (SELECT COUNT(*) FROM PRODUCT) AS METRIC_VALUE, '' AS REGION FROM DUAL"
                                + " UNION"
                                + " SELECT 'categoryCount', (SELECT COUNT(*) FROM (SELECT DISTINCT CATEGORY FROM PRODUCT_CATEGORY AS T1) AS T2), '' FROM DUAL"
                                + " UNION"
                                + " SELECT 'dateStarted', MIN(DATE_STARTED), REGION FROM APP_INSTANCE WHERE LAST_HEARTBEAT >= :MIN_HEARTBEAT_TIME GROUP BY REGION"
                                + " UNION"
                                + " SELECT 'productReviewCount', COUNT(*), REGION FROM PRODUCT_REVIEW GROUP BY REGION"
                                + " UNION"
                                + " SELECT 'customerCount', COUNT(*), REGION FROM CUSTOMER GROUP BY REGION"
                                + " UNION"
                                + " SELECT 'activeCustomerCount', COUNT(*), REGION FROM CUSTOMER WHERE DATE_LAST_ACTIVE >= :MIN_ACTIVE_TIME GROUP BY REGION"
                                + " UNION"
                                + " SELECT 'activeWebCustomerCount', COUNT(*), REGION FROM CUSTOMER WHERE WORKLOAD IS NULL AND DATE_LAST_ACTIVE >= :MIN_ACTIVE_TIME GROUP BY REGION"
                                + " UNION"
                                + " SELECT 'cartItemCount', SUM(QUANTITY), REGION FROM CART_SELECTION GROUP BY REGION"
                                + " UNION"
                                + " SELECT 'cartValue', SUM(CAST(QUANTITY AS DECIMAL(16,2)) * UNIT_PRICE), REGION FROM CART_SELECTION GROUP BY REGION"
                                + " UNION"
                                + " SELECT 'purchaseCount', COUNT(*), REGION FROM PURCHASE GROUP BY REGION"
                                + " UNION"
                                + " SELECT 'purchaseItemCount', SUM(QUANTITY), REGION FROM PURCHASE_SELECTION PS INNER JOIN PURCHASE P ON PS.PURCHASE_ID = P.ID GROUP BY REGION"
                                + " UNION"
                                + " SELECT 'purchaseValue', SUM(CAST(QUANTITY AS DECIMAL(16,2)) * UNIT_PRICE), REGION FROM PURCHASE_SELECTION PS INNER JOIN PURCHASE P ON PS.PURCHASE_ID = P.ID GROUP BY REGION");
        setStatsParameters(query, maxCustomerIdleTimeSec);

        // Fill stats
        for (Object[] row : (List<Object[]>) query.list()) {
            String metric = row[0].toString();
            Object value = row[1];
            String region = (String) row[2];

            if (region == null) {
                // A NULL region represents data from a previous Storefront version. Just associate it with the global stats.
                region = "";
            }

            StorefrontStats regionStats = regionStatsMap.get(region);
            if (regionStats == null) {
                regionStats = new StorefrontStats();
                regionStatsMap.put(region, regionStats);
            }

            if (metric.equals("productCount")) {
                regionStats.setProductCount(getIntValue(value));
            } else if (metric.equals("categoryCount")) {
                regionStats.setCategoryCount(getIntValue(value));
            } else if (metric.equals("dateStarted")) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(getLongValue(value));
                regionStats.setDateStarted(cal);
            } else if (metric.equals("productReviewCount")) {
                regionStats.setProductReviewCount(getIntValue(value));
            } else if (metric.equals("customerCount")) {
                regionStats.setCustomerCount(getIntValue(value));
            } else if (metric.equals("activeCustomerCount")) {
                regionStats.setActiveCustomerCount(getIntValue(value));
            } else if (metric.equals("activeWebCustomerCount")) {
                regionStats.setActiveWebCustomerCount(getIntValue(value));
            } else if (metric.equals("cartItemCount")) {
                regionStats.setCartItemCount(getIntValue(value));
            } else if (metric.equals("cartValue")) {
                regionStats.setCartValue(new BigDecimal(toNumericString(value)));
            } else if (metric.equals("purchaseCount")) {
                regionStats.setPurchaseCount(getIntValue(value));
            } else if (metric.equals("purchaseItemCount")) {
                regionStats.setPurchaseItemCount(getIntValue(value));
            } else if (metric.equals("purchaseValue")) {
                regionStats.setPurchaseValue(new BigDecimal(toNumericString(value)));
            } else {
                throw new RuntimeException("Unexpected metric: " + metric);
            }
        }

        return regionStatsMap;
    }

    protected static int getIntValue(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return Integer.valueOf(value.toString());
    }

    protected static BigDecimal getBigDecimalValue(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof Number) {
            return new BigDecimal(((Number) value).doubleValue());
        }
        String str = value.toString();
        if (str.equalsIgnoreCase("NaN")) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value.toString());
    }

    protected static long getLongValue(Object value) {
        return getBigDecimalValue(value).longValue();
    }

    @Override
    public int deleteDeadAppInstances(Calendar maxLastHeartbeat) {
        SQLQuery query = getSession().createSQLQuery("DELETE FROM APP_INSTANCE WHERE LAST_HEARTBEAT <= :MAX_LAST_HEARTBEAT");
        query.setParameter("MAX_LAST_HEARTBEAT", maxLastHeartbeat);
        return query.executeUpdate();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<DbNode> getDbNodes() {
        SQLQuery query = getSession()
                .createSQLQuery("SELECT SYSTEM.NODES.*, CASE WHEN ID = GETNODEID() THEN 1 ELSE 0 END AS LOCAL FROM SYSTEM.NODES");
        query.setResultTransformer(new ResultTransformer() {
            private static final long serialVersionUID = 211285415624172491L;

            @SuppressWarnings("rawtypes")
            @Override
            public List transformList(List collection) {
                return collection;
            }

            @Override
            public Object transformTuple(Object[] tuple, String[] aliases) {
                DbNode node = new DbNode();
                for (int i = 0; i < aliases.length; i++) {
                    String alias = aliases[i].toLowerCase();
                    if (alias.equals("id")) {
                        node.setId((Integer) tuple[i]);
                    } else if (alias.equals("localid")) {
                        node.setLocalId((Integer) tuple[i]);
                    } else if (alias.equals("port")) {
                        node.setPort((Integer) tuple[i]);
                    } else if (alias.equals("address")) {
                        node.setAddress((String) tuple[i]);
                    } else if (alias.equals("state")) {
                        node.setState((String) tuple[i]);
                    } else if (alias.equals("type")) {
                        node.setType((String) tuple[i]);
                    } else if (alias.equals("connstate")) {
                        node.setConnState((String) tuple[i]);
                    } else if (alias.equals("msgqsize")) {
                        node.setMsgQSize((Integer) tuple[i]);
                    } else if (alias.equals("triptime")) {
                        node.setTripTime((Integer) tuple[i]);
                    } else if (alias.equals("georegion")) {
                        node.setGeoRegion((String) tuple[i]);
                    } else if (alias.equals("local")) {
                        node.setLocal(tuple[i].toString().equals("1"));
                    }
                }
                return node;
            }
        });
        return (List<DbNode>) query.list();
    }

    protected void setStatsParameters(SQLQuery query, int maxCustomerIdleTimeSec) {
        Calendar now = Calendar.getInstance();

        // MIN_ACTIVE_TIME
        Calendar minActiveTime = (Calendar) now.clone();
        minActiveTime.add(Calendar.SECOND, -maxCustomerIdleTimeSec);
        query.setParameter("MIN_ACTIVE_TIME", minActiveTime);

        // MIN_HEARTBEAT_TIME
        Calendar minHeartbeatTime = (Calendar) now.clone();
        minHeartbeatTime.add(Calendar.SECOND, -HeartbeatService.MAX_HEARTBEAT_AGE_SEC);
        query.setParameter("MIN_HEARTBEAT_TIME", minHeartbeatTime);
    }

    protected static String toNumericString(Object o) {
        if (o != null) {
            String str = o.toString();
            if (str.length() > 0 && !str.equalsIgnoreCase("NaN")) {
                return str;
            }
        }
        return "0";
    }

    protected SQLQuery buildProductQuery(ProductFilter filter, boolean countOnly) {
        StringBuilder sql = new StringBuilder();
        Map<String, Object> params = new HashMap<String, Object>();

        if (countOnly) {
            sql.append("SELECT COUNT(*) FROM PRODUCT WHERE 1=1");
        } else {
            sql.append("SELECT * FROM PRODUCT WHERE 1=1");
        }

        // Set match text
        String matchText = filter.getMatchText();
        if (matchText != null && !matchText.isEmpty()) {
            matchText = "%" + matchText.trim().toLowerCase() + "%";
            sql.append(" AND (LOWER(NAME) LIKE :MATCH_TEXT OR LOWER(DESCRIPTION) LIKE :MATCH_TEXT)");
            params.put("MATCH_TEXT", matchText);
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
            sql.append(" AND ID IN (SELECT PRODUCT_ID FROM PRODUCT_CATEGORY WHERE CATEGORY IN (" + categoryParamList + "))");
        }

        // Set sort
        ProductSort sort = filter.getSort();
        if (sort != null) {
            switch (sort) {
                case AVG_CUSTOMER_REVIEW:
                    sql.append(" ORDER BY COALESCE(RATING, -1) DESC, REVIEW_COUNT DESC");
                    break;

                case DATE_CREATED:
                    sql.append(" ORDER BY DATE_ADDED DESC");
                    break;

                case NEW_AND_POPULAR:
                    sql.append(" ORDER BY PURCHASE_COUNT DESC, DATE_ADDED DESC");
                    break;

                case PRICE_HIGH_TO_LOW:
                    sql.append(" ORDER BY UNIT_PRICE DESC");
                    break;

                case PRICE_LOW_TO_HIGH:
                    sql.append(" ORDER BY UNIT_PRICE");
                    break;

                case RELEVANCE:
                    if (matchText != null && !matchText.isEmpty()) {
                        sql.append(" ORDER BY CASE WHEN LOWER(NAME) LIKE :MATCH_TEXT THEN 1 ELSE 0 END DESC, NAME, DATE_ADDED DESC");
                    } else {
                        sql.append(" ORDER BY NAME, DATE_ADDED DESC");
                    }
                    break;

                default:
                    sql.append(" ORDER BY ID");
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
                s_transactionStatsMap.put(transactionName, stats = new TransactionStats());
            }
            stats.incrementCount(transactionName, System.currentTimeMillis() - startTimeMs, success);
        }
    }
}
