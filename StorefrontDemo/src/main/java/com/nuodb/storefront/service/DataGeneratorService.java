package com.nuodb.storefront.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import com.nuodb.storefront.dal.IStorefrontDao;
import com.nuodb.storefront.dal.TransactionType;
import com.nuodb.storefront.model.Customer;
import com.nuodb.storefront.model.Product;
import com.nuodb.storefront.model.ProductReview;

public class DataGeneratorService implements IDataGeneratorService {
    private final Random rnd = new Random();
    private final IStorefrontDao dao;
    private final List<Customer> customers = new ArrayList<Customer>();
    private int numCustomers = 100;
    private int numProducts = 1000;
    private int maxCategories = 2;
    private int maxReviewsPerProduct = 10;

    public DataGeneratorService(IStorefrontDao dao) {
        this.dao = dao;
    }

    @Override
    public int getNumCustomers() {
        return numCustomers;
    }

    @Override
    public void setNumCustomers(int numCustomers) {
        this.numCustomers = numCustomers;
    }

    @Override
    public int getNumProducts() {
        return numProducts;
    }

    @Override
    public void setNumProducts(int numProducts) {
        this.numProducts = numProducts;
    }

    @Override
    public int getMaxCategories() {
        return maxCategories;
    }

    @Override
    public void setMaxCategories(int maxCategories) {
        this.maxCategories = maxCategories;
    }

    @Override
    public int getMaxReviewsPerProduct() {
        return maxReviewsPerProduct;
    }

    @Override
    public void setMaxReviewsPerProduct(int maxReviewsPerProduct) {
        this.maxReviewsPerProduct = maxReviewsPerProduct;
    }

    @Override
    public void generate() {
        dao.runTransaction(TransactionType.READ_WRITE, new Runnable() {
            @Override
            public void run() {
                createCustomers();
                createProducts();
                customers.clear();
            }
        });
    }

    protected void createCustomers() {
        for (int i = 0; i < numCustomers; i++) {
            customers.add(createCustomer());
        }
    }

    protected Customer createCustomer() {
        Customer customer = new Customer();
        Calendar now = Calendar.getInstance();
        customer.setDateAdded(now);
        customer.setDateLastActive(now);
        customer.setEmailAddress("test" + rnd.nextInt(100000) + "@test.com");
        dao.save(customer);
        return customer;
    }

    protected void createProducts() {
        for (int i = 0; i < numProducts; i++) {
            createProduct();
        }
    }

    protected Product createProduct() {
        Calendar now = Calendar.getInstance();

        // Define product
        Product product = new Product();
        product.setName("Product " + now.getTimeInMillis());
        product.setDateAdded(now);
        product.setDateModified(now);
        product.setDescription("This is the description for this product.");
        product.setUnitPrice(BigDecimal.valueOf(rnd.nextInt(10000), 2));

        // Add reviews
        for (int i = 0; i < rnd.nextInt(maxReviewsPerProduct); i++) {
            addProductReview(product);
        }

        // Add categories
        for (int i = 0; i < rnd.nextInt(maxCategories) + 1; i++) {
            product.getCategories().add("Category " + (char) ('A' + rnd.nextInt(26)));
        }

        dao.save(product);
        return product;
    }

    protected void addProductReview(Product product) {
        Calendar now = Calendar.getInstance();
        ProductReview review = new ProductReview();
        review.setDateAdded(now);
        review.setTitle("This is a review");
        review.setComments("These are some comments on this product.");
        review.setRating(rnd.nextInt(5) + 1);
        review.setCustomer(pickRandomCustomer());
        product.addReview(review);
    }

    protected Customer pickRandomCustomer() {
        return (customers.isEmpty()) ? null : customers.get(rnd.nextInt(customers.size()));
    }
}
