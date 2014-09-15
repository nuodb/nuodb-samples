/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.model.entity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Index;

@Entity
public class Customer extends AutoIdEntity {
    @OneToMany(cascade = { CascadeType.ALL }, orphanRemoval = true, mappedBy = "customer")
    @OrderBy("dateAdded")
    private List<CartSelection> cartSelections = new ArrayList<CartSelection>();

    private String emailAddress;

    @NotNull
    private Calendar dateAdded;

    @NotNull
    @Index(name = "idx_customer_date_last_active")
    private Calendar dateLastActive;

    private transient int cartItemCount;

    /**
     * The region through which this customer last interacted with the store.
     */
    @NotNull
    private String region;

    /**
     * The name of the workload that created this simulated customer, or <code>null</code> for a "real" customer.
     */
    private String workload;

    public Customer() {
    }

    public List<CartSelection> getCartSelections() {
        return cartSelections;
    }
    
    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public Calendar getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Calendar dateAdded) {
        this.dateAdded = dateAdded;
    }

    public Calendar getDateLastActive() {
        return dateLastActive;
    }

    public void setDateLastActive(Calendar dateLastActive) {
        this.dateLastActive = dateLastActive;
    }

    public void addCartSelection(CartSelection selection) {
        selection.setCustomer(this);
        cartSelections.add(selection);
    }

    public void clearCartSelections() {
        cartSelections = null;
    }

    public int getCartItemCount() {
        return cartItemCount;
    }

    public void setCartItemCount(int cartItemCount) {
        this.cartItemCount = cartItemCount;
    }

    public String getDisplayName() {
        if (emailAddress != null && !emailAddress.isEmpty()) {
            return emailAddress;
        }
        return "Customer " + getId();
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getWorkload() {
        return workload;
    }

    public void setWorkload(String workload) {
        this.workload = workload;
    }
}
