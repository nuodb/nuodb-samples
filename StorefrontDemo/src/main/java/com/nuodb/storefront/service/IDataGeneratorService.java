package com.nuodb.storefront.service;

public interface IDataGeneratorService {

    public int getNumCustomers();

    public void setNumCustomers(int numCustomers);

    public int getNumProducts();

    public void setNumProducts(int numProducts);

    public int getMaxCategories();

    public void setMaxCategories(int maxCategories);

    public int getMaxReviewsPerProduct();

    public void setMaxReviewsPerProduct(int maxReviewsPerProduct);

    public void generate();

}