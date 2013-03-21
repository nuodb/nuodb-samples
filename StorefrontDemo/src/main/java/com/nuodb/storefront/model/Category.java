package com.nuodb.storefront.model;

public class Category {
    private String name;
    private int numProducts;

    public Category() {
    }

    public Category(String name, int numProducts) {
        this.name = name;
        this.numProducts = numProducts;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumProducts() {
        return numProducts;
    }

    public void setNumProducts(int numProducts) {
        this.numProducts = numProducts;
    }
}
