/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.service.storefront;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.googlecode.genericdao.search.Filter;
import com.googlecode.genericdao.search.Search;
import com.googlecode.genericdao.search.SearchResult;
import com.nuodb.storefront.StorefrontApp;
import com.nuodb.storefront.dal.IStorefrontDao;
import com.nuodb.storefront.dal.StorefrontDao;
import com.nuodb.storefront.dal.TransactionType;
import com.nuodb.storefront.exception.CartEmptyException;
import com.nuodb.storefront.exception.CustomerNotFoundException;
import com.nuodb.storefront.exception.ProductNotFoundException;
import com.nuodb.storefront.model.dto.Category;
import com.nuodb.storefront.model.dto.ProductFilter;
import com.nuodb.storefront.model.dto.ProductReviewFilter;
import com.nuodb.storefront.model.dto.StorefrontStats;
import com.nuodb.storefront.model.dto.TransactionStats;
import com.nuodb.storefront.model.dto.Workload;
import com.nuodb.storefront.model.entity.AppInstance;
import com.nuodb.storefront.model.entity.Cart;
import com.nuodb.storefront.model.entity.CartSelection;
import com.nuodb.storefront.model.entity.Customer;
import com.nuodb.storefront.model.entity.Product;
import com.nuodb.storefront.model.entity.ProductReview;
import com.nuodb.storefront.model.entity.Purchase;
import com.nuodb.storefront.model.entity.PurchaseSelection;
import com.nuodb.storefront.service.IStorefrontService;

/**
 * Basic implementation of the storefront service interface. Each service method invocation runs in its own transaction.
 */
public class StorefrontService implements IStorefrontService {
    private final AppInstance appInstance;
    private final IStorefrontDao dao;

    static {
        StorefrontDao.registerTransactionNames(new String[] { "addProduct", "addProductReview", "addToCart", "checkout", "getAppInstances",
                "getCategories", "getCustomerCart", "getDbNodes", "getOrCreateCustomer", "getProductDetails", "getProductReviews", "getProducts",
                "getStorefrontStats",
                "updateCart" });
    }

    public StorefrontService(AppInstance appInstance, IStorefrontDao dao) {
        this.appInstance = appInstance;
        this.dao = dao;        
    }
    
    @Override
    public AppInstance getAppInstance() {
        // TODO Auto-generated method stub
        return appInstance;
    }

    @Override
    public SearchResult<Category> getCategories() {
        return dao.runTransaction(TransactionType.READ_ONLY, "getCategories", new Callable<SearchResult<Category>>() {
            @Override
            public SearchResult<Category> call() throws Exception {
                return dao.getCategories();
            }
        });
    }

    @Override
    public SearchResult<Product> getProducts(final ProductFilter filter) {
        return dao.runTransaction(TransactionType.READ_ONLY, "getProducts", new Callable<SearchResult<Product>>() {
            @Override
            public SearchResult<Product> call() throws Exception {
                SearchResult<Product> result = dao.getProducts(filter);

                for (Product product : result.getResult()) {
                    dao.evict(product);
                    product.clearCategories();
                }

                return result;
            }
        });
    }

    @Override
    public SearchResult<ProductReview> getProductReviews(final ProductReviewFilter filter) {
        return dao.runTransaction(TransactionType.READ_ONLY, "getProductReviews", new Callable<SearchResult<ProductReview>>() {
            @Override
            public SearchResult<ProductReview> call() throws Exception {
                // Build search
                Search search = new Search();
                search.setSearchClass(ProductReview.class);
                search.addSort("dateAdded", true);
                search.addFetch("customer");
                if (filter.getPage() != null && filter.getPage() > 0) {
                    search.setPage(filter.getPage() - 1);
                }
                if (filter.getPageSize() != null) {
                    search.setMaxResults(filter.getPageSize());
                }
                if (filter.getProductId() != null) {
                    search.addFilterEqual("product.id", filter.getProductId());
                }

                // Do search
                @SuppressWarnings("unchecked")
                SearchResult<ProductReview> result = dao.searchAndCount(search);

                // Prep results
                for (ProductReview review : result.getResult()) {
                    dao.evict(review);
                    review.getCustomer().clearCartSelections();
                    review.setProduct(null);
                }
                return result;
            }
        });
    }

    @Override
    public Product getProductDetails(final long productId) {
        return dao.runTransaction(TransactionType.READ_ONLY, "getProductDetails", new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Search productSearch = new Search(Product.class);
                productSearch.addFilterEqual("id", productId);
                productSearch.addFetch("categories");
                Product product = (Product) dao.searchUnique(productSearch);
                if (product == null) {
                    throw new ProductNotFoundException();
                }

                dao.evict(product);
                return product;
            }
        });
    }

    @Override
    public Product addProduct(final String name, final String description, final String imageUrl, final BigDecimal unitPrice,
            final Collection<String> categories) {
        if (unitPrice.signum() < 0) {
            throw new IllegalArgumentException("unitPrice");
        }

        return dao.runTransaction(TransactionType.READ_WRITE, "addProduct", new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Calendar now = Calendar.getInstance();
                Product product = new Product();
                product.setName(name);
                product.setDescription(description);
                product.setDateAdded(now);
                product.setDateModified(now);
                product.setImageUrl(imageUrl);
                product.setUnitPrice(unitPrice);
                product.getCategories().addAll(categories);

                dao.save(product);
                dao.evict(product);
                return product;
            }
        });
    }

    @Override
    public ProductReview addProductReview(final long customerId, final long productId, final String title, final String comments,
            final String emailAddress, final int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("rating");
        }

        return dao.runTransaction(TransactionType.READ_WRITE, "addProductReview", new Callable<ProductReview>() {
            @Override
            public ProductReview call() throws Exception {

                Customer customer = dao.find(Customer.class, customerId);
                if (customer == null) {
                    throw new CustomerNotFoundException();
                }

                Search productSearch = new Search(Product.class);
                productSearch.addFilterEqual("id", productId);
                Product product = (Product) dao.searchUnique(productSearch);
                if (product == null) {
                    throw new ProductNotFoundException();
                }

                // Add review
                Calendar now = Calendar.getInstance();
                ProductReview review = new ProductReview();
                review.setCustomer(customer);
                review.setTitle(title);
                review.setComments(comments);
                review.setRating(rating);
                review.setDateAdded(now);
                review.setRegion(appInstance.getRegion());
                review.setProduct(product);
                dao.save(review);

                // Update product
                product.addRating(rating);
                dao.save(product);

                // Update customer
                if (emailAddress != null && !emailAddress.isEmpty()) {
                    customer.setEmailAddress(emailAddress);
                    dao.save(customer);
                }

                dao.evict(review);
                review.setCustomer(null);
                review.setProduct(null);
                return review;
            }
        });
    }

    @Override
    public Customer getOrCreateCustomer(final long customerId, final Workload workload) {
        return dao.runTransaction(TransactionType.READ_WRITE, "getOrCreateCustomer", new Callable<Customer>() {
            @Override
            public Customer call() throws Exception {

                Calendar now = Calendar.getInstance();
                Customer customer = dao.find(Customer.class, customerId);
                if (customer == null) {
                    customer = new Customer();
                    customer.setDateAdded(now);
                }
                customer.setWorkload((workload == null) ? null : workload.getName());
                customer.setRegion(appInstance.getRegion());
                customer.setDateLastActive(now);
                customer.setRegion(appInstance.getRegion());
                countCartItems(customer);

                dao.save(customer);
                dao.flush();
                dao.evict(customer);
                customer.clearCartSelections();
                return customer;
            }
        });
    }

    @Override
    public Cart getCustomerCart(final long customerId) {
        return dao.runTransaction(TransactionType.READ_ONLY, "getCustomerCart", new Callable<Cart>() {
            @Override
            public Cart call() throws Exception {
                Search search = new Search(Customer.class);
                search.addFilterEqual("id", customerId);
                search.addFetch("cartSelections");

                Customer customer = (Customer) dao.searchUnique(search);
                if (customer == null) {
                    throw new CustomerNotFoundException();
                }

                BigDecimal totalPrice = new BigDecimal(0);
                for (CartSelection selection : customer.getCartSelections()) {
                    selection.clearCustomer();

                    Product product = selection.getProduct();
                    product.clearCategories();
                    totalPrice = totalPrice.add(selection.getUnitPrice().multiply(BigDecimal.valueOf(selection.getQuantity())));
                }

                Cart result = new Cart();
                result.setResult(customer.getCartSelections());
                result.setTotalCount(customer.getCartSelections().size());
                result.setTotalPrice(totalPrice);
                return result;
            }
        });
    }

    @Override
    public int addToCart(final long customerId, final long productId, final int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity");
        }

        return dao.runTransaction(TransactionType.READ_WRITE, "addToCart", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Customer customer = dao.find(Customer.class, customerId);
                if (customer == null) {
                    throw new CustomerNotFoundException();
                }

                Product product = dao.find(Product.class, productId);
                if (product == null) {
                    throw new ProductNotFoundException();
                }

                addOrUpdateCartItem(customer, product, quantity, true);
                dao.save(customer);
                return countCartItems(customer);
            }
        });
    }

    @Override
    public int updateCart(final long customerId, final Map<Long, Integer> productQuantityMap) throws IllegalArgumentException,
            CustomerNotFoundException, ProductNotFoundException {
        return dao.runTransaction(TransactionType.READ_WRITE, "updateCart", new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Customer customer = dao.find(Customer.class, customerId);
                if (customer == null) {
                    throw new CustomerNotFoundException();
                }

                List<CartSelection> cart = customer.getCartSelections();
                if (productQuantityMap == null || productQuantityMap.isEmpty()) {
                    // There's nothing in the map so remove all items
                    cart.clear();
                } else {
                    // Add/update items described in the map
                    Set<CartSelection> referencedItems = new HashSet<CartSelection>();
                    for (Map.Entry<Long, Integer> productQuantity : productQuantityMap.entrySet()) {
                        long productId = productQuantity.getKey();
                        int quantity = productQuantity.getValue();
                        Product product = dao.find(Product.class, productId);
                        if (product == null) {
                            throw new ProductNotFoundException();
                        }

                        referencedItems.add(addOrUpdateCartItem(customer, product, quantity, false));
                    }

                    // Remove items not described in the map
                    for (int i = cart.size() - 1; i >= 0; i--) {
                        if (!referencedItems.contains(cart.get(i))) {
                            cart.remove(i);
                        }
                    }
                }
                dao.save(customer);
                return countCartItems(customer);
            }
        });
    }

    @Override
    public Purchase checkout(final long customerId) {
        return dao.runTransaction(TransactionType.READ_WRITE, "checkout", new Callable<Purchase>() {
            @Override
            public Purchase call() throws Exception {
                Customer customer = dao.find(Customer.class, customerId);
                if (customer == null) {
                    throw new CustomerNotFoundException();
                }

                List<CartSelection> cart = customer.getCartSelections();
                if (cart.isEmpty()) {
                    throw new CartEmptyException();
                }

                // Initialize transaction
                Calendar now = Calendar.getInstance();
                Purchase transaction = new Purchase();
                transaction.setDatePurchased(now);
                transaction.setRegion(appInstance.getRegion());
                transaction.setCustomer(customer);

                // Move items from cart to transaction
                for (CartSelection cartSelection : cart) {
                    PurchaseSelection selection = new PurchaseSelection(cartSelection);
                    transaction.addTransactionSelection(selection);
                    selection.setRegion(appInstance.getRegion());
                    selection.setUnitPrice(selection.getProduct().getUnitPrice());

                    // Increment purchase count. This is denormalized,
                    // non-synchronized data so it may not be 100% accurate.
                    // But that's ok -- it's just use to roughly gauge
                    // popularity and can be reconstructed exactly later
                    // by looking at the transaction table.
                    Product product = selection.getProduct();
                    product.setPurchaseCount(product.getPurchaseCount() + selection.getQuantity());
                    dao.save(product);
                }
                customer.getCartSelections().clear();

                dao.save(customer); // for cart clearing
                dao.save(transaction);
                return transaction;
            }
        });
    }

    @Override
    public Map<String, TransactionStats> getTransactionStats() {
        return dao.getTransactionStats();
    }

    @Override
    public StorefrontStats getStorefrontStats(final int maxCustomerIdleTimeSec, final Integer maxAgeSec) {
        return dao.runTransaction(TransactionType.READ_ONLY, "getStorefrontStats", new Callable<StorefrontStats>() {
            @Override
            public StorefrontStats call() {
                return dao.getStorefrontStats(maxCustomerIdleTimeSec, maxAgeSec);
            }
        });
    }

    @Override
    public Map<String, StorefrontStats> getStorefrontStatsByRegion(final int maxCustomerIdleTimeSec) {
        return dao.runTransaction(TransactionType.READ_ONLY, "getStorefrontStats", new Callable<Map<String, StorefrontStats>>() {
            @Override
            public Map<String, StorefrontStats> call() {
                return dao.getStorefrontStatsByRegion(maxCustomerIdleTimeSec);
            }
        });
    }

    @Override
    public List<AppInstance> getAppInstances(final boolean activeOnly) {
        return dao.runTransaction(TransactionType.READ_ONLY, "getAppInstances", new Callable<List<AppInstance>>() {
            @SuppressWarnings("unchecked")
            @Override
            public List<AppInstance> call() {
                Search search = new Search(AppInstance.class);
                if (activeOnly) {
                    Calendar minLastHeartbeat = Calendar.getInstance();
                    minLastHeartbeat.add(Calendar.SECOND, -StorefrontApp.MAX_HEARTBEAT_AGE_SEC);
                    search.addFilter(Filter.greaterOrEqual("lastHeartbeat", minLastHeartbeat));
                }
                search.addSort("region", false);
                search.addSort("url", false);
                search.addSort("lastHeartbeat", true);
                List<AppInstance> instances = (List<AppInstance>) dao.search(search);

                // Perform instance list cleanup:
                // 1) For the local instance, use in-memory object (newer) rather than what's in DB (updated with every heartbeat)
                // 2) Remove extra instances with the same URL (instance with most recent heartbeat wins)
                String localUuid = appInstance.getUuid();
                boolean foundLocal = false;
                for (int i = 0; i < instances.size();) {
                    AppInstance instance = instances.get(i);
                    if (instance.getUuid().equals(localUuid)) {
                        instances.set(i, appInstance);
                        foundLocal = true;
                    } else if (activeOnly && i > 0 && instance.getUrl().equals(instances.get(i - 1).getUrl())) {
                        instances.remove(i);
                        continue;
                    }

                    i++;
                }

                if (!foundLocal) {
                    // Avoid race condition whereby the instance list is being requested before the first heartbeat
                    // by ensuring the local instance is always present in the list
                    instances.add(appInstance);
                }

                return instances;
            }
        });
    }

    protected int countCartItems(Customer customer) {
        int cartItemCount = 0;
        for (CartSelection selection : customer.getCartSelections()) {
            cartItemCount += selection.getQuantity();
        }
        customer.setCartItemCount(cartItemCount);
        return cartItemCount;
    }

    protected CartSelection addOrUpdateCartItem(Customer customer, Product product, int quantity, boolean incrementQty) {
        Calendar now = Calendar.getInstance();
        long productId = product.getId();

        List<CartSelection> cart = customer.getCartSelections();
        CartSelection modifiedItem = null;
        for (int i = cart.size() - 1; i >= 0; i--) {
            CartSelection selection = cart.get(i);

            if (selection.getProduct().getId() == productId) {
                modifiedItem = selection;
                if (incrementQty) {
                    modifiedItem.setQuantity(selection.getQuantity() + quantity);
                } else {
                    modifiedItem.setQuantity(quantity);
                }
                if (modifiedItem.getQuantity() <= 0) {
                    cart.remove(i);
                }
            }
        }

        if (modifiedItem == null && quantity >= 0) {
            modifiedItem = new CartSelection();
            modifiedItem.setDateAdded(now);
            modifiedItem.setProduct(product);
            modifiedItem.setQuantity(quantity);
            customer.addCartSelection(modifiedItem);
        }

        modifiedItem.setUnitPrice(product.getUnitPrice());
        modifiedItem.setDateModified(now);
        modifiedItem.setRegion(appInstance.getRegion());
        return modifiedItem;
    }
}
