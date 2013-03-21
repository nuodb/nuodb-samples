package com.nuodb.storefront.service;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
import com.nuodb.storefront.model.ProductReview;
import com.nuodb.storefront.model.Transaction;
import com.nuodb.storefront.model.TransactionSelection;

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
    public SearchResult<Product> getProducts(final String matchText, final Collection<String> categories, final Integer page, final Integer pageSize,
            final ProductSort sort) {
        return dao.runTransaction(TransactionType.READ_ONLY, new Callable<SearchResult<Product>>() {
            @Override
            public SearchResult<Product> call() throws Exception {
                SearchResult<Product> result = dao.getProducts(matchText, categories, page, pageSize, sort);

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
            throw new IllegalArgumentException("quantity");
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

                product.addReview(review);
                dao.save(product);

                if (emailAddress != null && emailAddress.isEmpty()) {
                    customer.setEmailAddress(emailAddress);
                    dao.save(customer);
                }

                return review;
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
        if (quantity < 0) {
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

                Calendar now = Calendar.getInstance();
                List<CartSelection> cart = customer.getCartSelections();
                CartSelection selection = null;
                int totalItems = 0;
                for (int i = cart.size() - 1; i >= 0; i--) {
                    selection = customer.getCartSelections().get(i);

                    if (selection.getProduct().getId() == productId) {
                        if (quantity == 0) {
                            cart.remove(i);
                        }
                        selection.setQuantity(quantity);
                    }

                    totalItems += selection.getQuantity();
                    selection = null;
                }

                if (selection == null) {
                    selection = new CartSelection();
                    selection.setDateAdded(now);
                    selection.setProduct(product);
                    selection.setQuantity(quantity);
                    totalItems += quantity;
                    customer.addCartSelection(selection);
                }

                selection.setUnitPrice(product.getUnitPrice());
                selection.setDateModified(now);
                dao.save(customer);
                return totalItems;
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

                Transaction transaction = new Transaction();
                for (CartSelection cartSelection : cart) {
                    TransactionSelection selection = new TransactionSelection(cartSelection);
                    transaction.addTransactionSelection(selection);
                    selection.setUnitPrice(selection.getProduct().getUnitPrice());
                }

                dao.save(customer);
                return transaction;
            }
        });
    }
}
