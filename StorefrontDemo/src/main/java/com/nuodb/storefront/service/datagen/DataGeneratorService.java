/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.service.datagen;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.hibernate.StatelessSession;

import com.nuodb.storefront.model.Customer;
import com.nuodb.storefront.model.Product;
import com.nuodb.storefront.model.ProductReview;
import com.nuodb.storefront.service.IDataGeneratorService;

public class DataGeneratorService implements IDataGeneratorService {
    private final StatelessSession session;

    public DataGeneratorService(StatelessSession session) {
        this.session = session;
    }

    @Override
    public void generateAll(int numCustomers, int numProducts, int maxCategoriesPerProduct, int maxReviewsPerProduct) throws IOException {
        try {
            DataGenerator gen = new DataGenerator();

            // Insert customers
            for (Customer customer : gen.createCustomers(numCustomers)) {
                session.insert(customer);
            }

            // Insert products
            List<Product> products = gen.createProducts(numProducts, maxCategoriesPerProduct, maxReviewsPerProduct);
            saveProducts(products);
        } catch (SQLException e) {
            throw new IOException(e);
        } finally {
            session.close();
        }
    }

    @Override
    public void generateProductReviews(int numCustomers, List<Product> products, int maxReviewsPerProduct) throws IOException {
        try {
            DataGenerator gen = new DataGenerator();

            // Insert customers
            for (Customer customer : gen.createCustomers(numCustomers)) {
                session.insert(customer);
            }
            
            // Insert products
            for (Product product : products) {
                Calendar now = Calendar.getInstance();
                product.setDateAdded(now);
                product.setDateModified(now);
                gen.addProductReviews(product, maxReviewsPerProduct);
            }
            
            saveProducts(products);
            
        } catch (SQLException e) {
            throw new IOException(e);
        } finally {
            session.close();
        }
    }
    
    protected void saveProducts(List<Product> products) throws SQLException {
        // Insert products
        for (Product product : products) {
            session.insert(product);

            // Insert product reviews
            for (ProductReview review : product.getReviews()) {
                session.insert(review);
            }
        }

        // Insert product categories
        StringBuilder buff = new StringBuilder();
        buff.append("insert into product_category (product_id, category) values ");
        int catCount = 0;
        for (Product product : products) {
            for (Iterator<String> iterator = product.getCategories().iterator(); iterator.hasNext();) {
                iterator.next();
                if (catCount++ > 0) {
                    buff.append(",");
                }
                buff.append("(?, ?)");
            }
        }
        PreparedStatement catStmt = session.connection().prepareStatement(buff.toString());
        int paramCount = 0;
        for (Product product : products) {
            for (String category : product.getCategories()) {
                catStmt.setInt(++paramCount, product.getId());
                catStmt.setString(++paramCount, category);
            }
        }
        catStmt.execute();
    }
}
