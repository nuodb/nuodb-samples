package com.nuodb.storefront.service;

public interface IDataGeneratorService {

    public abstract int getNumCustomers();

    public abstract void setNumCustomers(int numCustomers);

    public abstract int getNumProducts();

    public abstract void setNumProducts(int numProducts);

    public abstract int getMaxCategories();

    public abstract void setMaxCategories(int maxCategories);

    public abstract int getMaxReviewsPerProduct();

    public abstract void setMaxReviewsPerProduct(int maxReviewsPerProduct);

    public abstract void generate();

}