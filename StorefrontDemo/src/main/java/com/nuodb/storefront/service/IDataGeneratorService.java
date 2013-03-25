package com.nuodb.storefront.service;

import java.util.List;

import com.nuodb.storefront.model.Product;

public interface IDataGeneratorService {
    public void generate(int numCustomers, int numProducts, int maxCategoriesPerProduct, int maxReviewsPerProduct);
    
    public void generate(int numCustomers, List<Product> products, int maxReviewsPerProduct);
}