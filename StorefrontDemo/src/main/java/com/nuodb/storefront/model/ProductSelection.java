/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.model;

import java.math.BigDecimal;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

@MappedSuperclass
public abstract class ProductSelection {
    @Id
    @ManyToOne
    private Product product;

    private int quantity;

    @NotNull
    private Calendar dateAdded;

    @NotNull
    private Calendar dateModified;

    @Column(precision = 8, scale = 2)
    @NotNull
    private BigDecimal unitPrice;

    public ProductSelection() {
    }

    public ProductSelection(ProductSelection selection) {
        this.product = selection.product;
        this.quantity = selection.quantity;
        this.dateAdded = selection.dateAdded;
        this.dateModified = selection.dateModified;
        this.unitPrice = selection.unitPrice;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Calendar getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Calendar dateAdded) {
        this.dateAdded = dateAdded;
    }

    public Calendar getDateModified() {
        return dateModified;
    }

    public void setDateModified(Calendar dateModified) {
        this.dateModified = dateModified;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    /** Overridden because we're using a composite key. Default semantics are still fine for our use cases. */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /** Overridden because we're using a composite key. Default semantics are still fine for our use cases. */
    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
