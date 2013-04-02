package com.nuodb.storefront.service.storefront;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.googlecode.genericdao.search.Search;
import com.googlecode.genericdao.search.SearchResult;
import com.nuodb.storefront.dal.IStorefrontDao;
import com.nuodb.storefront.dal.TransactionType;
import com.nuodb.storefront.exception.CartEmptyException;
import com.nuodb.storefront.exception.CustomerNotFoundException;
import com.nuodb.storefront.exception.ProductNotFoundException;
import com.nuodb.storefront.model.Cart;
import com.nuodb.storefront.model.CartSelection;
import com.nuodb.storefront.model.Category;
import com.nuodb.storefront.model.Customer;
import com.nuodb.storefront.model.Product;
import com.nuodb.storefront.model.ProductFilter;
import com.nuodb.storefront.model.ProductReview;
import com.nuodb.storefront.model.Transaction;
import com.nuodb.storefront.model.TransactionSelection;
import com.nuodb.storefront.service.IStorefrontService;

/**
 * Basic implementation of the storefront service interface. Each service method invocation runs in its own transaction.
 */
public class StorefrontService implements IStorefrontService {
    private final IStorefrontDao dao;

    public StorefrontService(IStorefrontDao dao) {
        this.dao = dao;
    }

    @Override
    public SearchResult<Category> getCategories() {
        return dao.runTransaction(TransactionType.READ_ONLY, new Callable<SearchResult<Category>>() {
            @Override
            public SearchResult<Category> call() throws Exception {
                return dao.getCategories();
            }
        });
    }

    @Override
    public SearchResult<Product> getProducts(final ProductFilter filter) {
        return dao.runTransaction(TransactionType.READ_ONLY, new Callable<SearchResult<Product>>() {
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
    public Product getProductDetails(final int productId) {
        return dao.runTransaction(TransactionType.READ_ONLY, new Callable<Product>() {
            @Override
            public Product call() throws Exception {
                Search search = new Search(Product.class);
                // search.addFetches("categories");
                search.addFilterEqual("id", productId);
                search.addFetch("categories");
                Product product = (Product) dao.searchUnique(search);
                if (product == null) {
                    throw new ProductNotFoundException();
                }

                // Initialize customers associated with each review, but don't do a deep load
                Set<Customer> customers = new HashSet<Customer>();
                for (ProductReview review : product.getReviews()) {
                    customers.add(review.getCustomer());
                }
                for (Customer customer : customers) {
                    dao.initialize(customer);
                    dao.evict(customer);
                    customer.clearCartSelections();
                    customer.clearTransactions();
                }

                // Break circular references so items are serialized properly
                for (ProductReview review : product.getReviews()) {
                    dao.evict(review);
                    review.clearProduct();
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

        return dao.runTransaction(TransactionType.READ_WRITE, new Callable<Product>() {
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
                return product;
            }
        });
    }

    @Override
    public ProductReview addProductReview(final int customerId, final int productId, final String title, final String comments,
            final String emailAddress, final int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("rating");
        }

        return dao.runTransaction(TransactionType.READ_WRITE, new Callable<ProductReview>() {
            @Override
            public ProductReview call() throws Exception {

                Customer customer = dao.find(Customer.class, customerId);
                if (customer == null) {
                    throw new CustomerNotFoundException();
                }

                Product product = dao.find(Product.class, productId);
                if (product == null) {
                    throw new ProductNotFoundException();
                }

                Calendar now = Calendar.getInstance();
                ProductReview review = new ProductReview();
                review.setCustomer(customer);
                review.setTitle(title);
                review.setComments(comments);
                review.setRating(rating);
                review.setDateAdded(now);

                // Update and save product (cascading save to review)
                product.addReview(review);
                dao.save(product);

                if (emailAddress != null && !emailAddress.isEmpty()) {
                    customer.setEmailAddress(emailAddress);
                    dao.save(customer);
                }

                ProductReview standaloneRev = new ProductReview();
                standaloneRev.setId(review.getId());
                standaloneRev.setRating(review.getRating());
                standaloneRev.setComments(review.getComments());
                standaloneRev.setDateAdded(review.getDateAdded());
                standaloneRev.setTitle(review.getTitle());

                return standaloneRev;
            }
        });
    }

    @Override
    public Customer getOrCreateCustomer(final int customerId) {
        return dao.runTransaction(TransactionType.READ_WRITE, new Callable<Customer>() {
            @Override
            public Customer call() throws Exception {

                Calendar now = Calendar.getInstance();
                Customer customer = dao.find(Customer.class, customerId);
                if (customer == null) {
                    customer = new Customer();
                    customer.setDateAdded(now);
                }
                customer.setDateLastActive(now);
                countCartItems(customer);

                dao.save(customer);
                dao.evict(customer);
                customer.clearCartSelections();
                customer.clearTransactions();
                return customer;
            }
        });
    }

    @Override
    public Cart getCustomerCart(final int customerId) {
        return dao.runTransaction(TransactionType.READ_ONLY, new Callable<Cart>() {
            @Override
            public Cart call() throws Exception {
                Search search = new Search(Customer.class);
                search.addFilterEqual("id", customerId);
                search.addFetch("cartSelections");

                Customer customer = (Customer) dao.searchUnique(search);
                if (customer == null) {
                    throw new CustomerNotFoundException();
                }
                dao.evict(customer);
                customer.clearTransactions();

                BigDecimal totalPrice = new BigDecimal(0);
                for (CartSelection selection : customer.getCartSelections()) {
                    selection.clearCustomer();

                    Product product = selection.getProduct();
                    dao.evict(product);
                    product.clearCategories();
                    product.clearReviews();
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
    public int addToCart(final int customerId, final int productId, final int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("quantity");
        }

        return dao.runTransaction(TransactionType.READ_WRITE, new Callable<Integer>() {
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
    public int updateCart(final int customerId, final Map<Integer, Integer> productQuantityMap) throws IllegalArgumentException, CustomerNotFoundException,
            ProductNotFoundException {
        return dao.runTransaction(TransactionType.READ_WRITE, new Callable<Integer>() {
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
                    for (Map.Entry<Integer, Integer> productQuantity : productQuantityMap.entrySet()) {
                        int productId = productQuantity.getKey();
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
    public Transaction checkout(final int customerId) {
        return dao.runTransaction(TransactionType.READ_WRITE, new Callable<Transaction>() {
            @Override
            public Transaction call() throws Exception {
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
                Transaction transaction = new Transaction();
                transaction.setDatePurchased(now);
                customer.addTransaction(transaction);
                
                // Move items from cart to transaction
                for (CartSelection cartSelection : cart) {
                    TransactionSelection selection = new TransactionSelection(cartSelection);
                    transaction.addTransactionSelection(selection);
                    selection.setUnitPrice(selection.getProduct().getUnitPrice());
                    
                    // Increment purchase count.  This is denormalized, non-synchronized data so it may not be 100% accurate.
                    // But that's ok -- it's just use to roughly gauge populatory and can be reconstructed exactly later
                    // by looking at the transaction table.
                    Product product = selection.getProduct();
                    product.setPurchaseCount(product.getPurchaseCount() + selection.getQuantity());
                    dao.save(product);
                }
                customer.getCartSelections().clear();

                dao.save(customer);
                return transaction;
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
        int productId = product.getId();
        
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
        return modifiedItem;
    }
}
