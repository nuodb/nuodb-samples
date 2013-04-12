/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.service.datagen;

import java.util.Calendar;
import java.util.List;

import com.nuodb.storefront.dal.IStorefrontDao;
import com.nuodb.storefront.dal.TransactionType;
import com.nuodb.storefront.model.Product;
import com.nuodb.storefront.service.IDataGeneratorService;

public class DataGeneratorService implements IDataGeneratorService {
    private final IStorefrontDao dao;

    public DataGeneratorService(IStorefrontDao dao) {
        this.dao = dao;
    }

    @Override
    public void generateAll(final int numCustomers, final int numProducts, final int maxCategoriesPerProduct, final int maxReviewsPerProduct) {
        final DataGenerator gen = new DataGenerator(dao);
        dao.runTransaction(TransactionType.READ_WRITE, "generateAll", new Runnable() {
            @Override
            public void run() {
                gen.createCustomers(numCustomers);
                gen.createProducts(numProducts, maxCategoriesPerProduct, maxReviewsPerProduct);
            }
        });
    }

    @Override
    public void generateProductReviews(final int numCustomers, final List<Product> products, final int maxReviewsPerProduct) {
        final DataGenerator gen = new DataGenerator(dao);
        dao.runTransaction(TransactionType.READ_WRITE, "generateProductReviews", new Runnable() {
            @Override
            public void run() {
                gen.createCustomers(numCustomers);
                for (Product product : products) {
                    Calendar now = Calendar.getInstance();
                    product.setDateAdded(now);
                    product.setDateModified(now);
                    gen.addProductReviews(product, maxReviewsPerProduct);
                    dao.save(product);
                }
            }
        });
    }
}
