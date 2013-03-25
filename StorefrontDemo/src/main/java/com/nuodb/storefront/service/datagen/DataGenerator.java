package com.nuodb.storefront.service.datagen;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import com.nuodb.storefront.dal.IStorefrontDao;
import com.nuodb.storefront.model.Customer;
import com.nuodb.storefront.model.Product;
import com.nuodb.storefront.model.ProductReview;

public class DataGenerator {
    private final Random rnd = new Random();
    private final IStorefrontDao dao;
    private final List<Customer> customers = new ArrayList<Customer>();

    public DataGenerator(IStorefrontDao dao) {
        this.dao = dao;
    }

    public void createCustomers(int numCustomers) {
        for (int i = 0; i < numCustomers; i++) {
            customers.add(createCustomer());
        }
    }
    
    public void addProductReviews(Product product, int maxReviewsPerProduct) {
        for (int i = 0; i < rnd.nextInt(maxReviewsPerProduct); i++) {
            addProductReview(product);
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

    public void createProducts(int numProducts, int maxCategoriesPerProduct, int maxReviewsPerProduct) {
        for (int i = 0; i < numProducts; i++) {
            createProduct(maxCategoriesPerProduct, maxReviewsPerProduct);
        }
    }

    protected Product createProduct(int maxCategoriesPerProduct, int maxReviewsPerProduct) {
        Calendar now = Calendar.getInstance();

        // Define product
        Product product = new Product();
        product.setName("Product " + now.getTimeInMillis());
        product.setDateAdded(now);
        product.setDateModified(now);
        product.setDescription("This is the description for this product.");
        product.setUnitPrice(BigDecimal.valueOf(rnd.nextInt(10000), 2));

        // Add reviews
        addProductReviews(product, maxReviewsPerProduct);

        // Add categories
        for (int i = 0; i < rnd.nextInt(maxCategoriesPerProduct) + 1; i++) {
            product.getCategories().add("Category " + (char) ('A' + rnd.nextInt(26)));
        }

        dao.save(product);
        return product;
    }

    protected void addProductReview(Product product) {
        Calendar now = Calendar.getInstance();
        Customer customer = pickRandomCustomer();
        if (customer == null) {
            return;
        }
        
        ProductReview review = new ProductReview();
        review.setDateAdded(now);
        review.setTitle("This is a review");
        review.setComments("These are some comments on this product.");
        review.setRating(rnd.nextInt(5) + 1);
        review.setCustomer(customer);
        product.addReview(review);
    }

    protected Customer pickRandomCustomer() {
        return (customers.isEmpty()) ? null : customers.get(rnd.nextInt(customers.size()));
    }
}
