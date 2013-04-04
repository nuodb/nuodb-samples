package com.nuodb.storefront.model;

import java.math.BigDecimal;
import java.util.Calendar;

public class StorefrontStats {
    private Calendar timestamp;
    private String storefrontName;
    private long uptimeMs;
    private int productCount;
    private int categoryCount;
    private int productReviewCount;
    private int customerCount;
    private int activeCustomerCount;
    private int cartCount;
    private int cartItemCount;
    private BigDecimal cartValue;
    private int purchaseCount;
    private int purchaseItemCount;
    private BigDecimal purchaseValue;

    public StorefrontStats() {
    }

    public Calendar getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Calendar timestamp) {
        this.timestamp = timestamp;
    }

    public String getStorefrontName() {
        return storefrontName;
    }

    public void setStorefrontName(String storefrontName) {
        this.storefrontName = storefrontName;
    }

    public long getUptimeMs() {
        return uptimeMs;
    }

    public void setUptimeMs(long uptimeMs) {
        this.uptimeMs = uptimeMs;
    }

    public int getProductCount() {
        return productCount;
    }

    public void setProductCount(int productCount) {
        this.productCount = productCount;
    }

    public int getCategoryCount() {
        return categoryCount;
    }

    public void setCategoryCount(int categoryCount) {
        this.categoryCount = categoryCount;
    }

    public int getProductReviewCount() {
        return productReviewCount;
    }

    public void setProductReviewCount(int productReviewCount) {
        this.productReviewCount = productReviewCount;
    }

    public int getCustomerCount() {
        return customerCount;
    }

    public void setCustomerCount(int customerCount) {
        this.customerCount = customerCount;
    }

    public int getActiveCustomerCount() {
        return activeCustomerCount;
    }

    public void setActiveCustomerCount(int activeCustomerCount) {
        this.activeCustomerCount = activeCustomerCount;
    }

    public int getCartCount() {
        return cartCount;
    }

    public void setCartCount(int cartCount) {
        this.cartCount = cartCount;
    }

    public int getCartItemCount() {
        return cartItemCount;
    }

    public void setCartItemCount(int cartItemCount) {
        this.cartItemCount = cartItemCount;
    }

    public BigDecimal getCartValue() {
        return cartValue;
    }

    public void setCartValue(BigDecimal cartValue) {
        this.cartValue = cartValue;
    }

    public int getPurchaseCount() {
        return purchaseCount;
    }

    public void setPurchaseCount(int purchaseCount) {
        this.purchaseCount = purchaseCount;
    }

    public int getPurchaseItemCount() {
        return purchaseItemCount;
    }

    public void setPurchaseItemCount(int purchaseItemCount) {
        this.purchaseItemCount = purchaseItemCount;
    }

    public BigDecimal getPurchaseValue() {
        return purchaseValue;
    }

    public void setPurchaseValue(BigDecimal purchaseValue) {
        this.purchaseValue = purchaseValue;
    }
}
