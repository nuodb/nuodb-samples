/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.model.entity;

import java.util.Calendar;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import com.nuodb.storefront.util.MD5Util;

@Entity
@Table(name = "Product_Review")
public class ProductReview extends AutoIdEntity {
    @ManyToOne
    @JoinColumn(name = "product_id")
    @NotNull
    private Product product;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    @NotNull
    private Customer customer;

    private int rating;

    @NotNull
    private String title;

    private String comments;

    @NotNull
    private Calendar dateAdded;

    @NotNull
    private String region;

    public ProductReview() {
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public Calendar getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Calendar dateAdded) {
        this.dateAdded = dateAdded;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void clearProduct() {
        this.product = null;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getGravitarHash() {
        if (customer == null) {
            return null;
        }
        String email = customer.getEmailAddress();
        if (email == null || email.isEmpty()) {
            return null;
        }
        return MD5Util.md5Hex(email);
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }
}
