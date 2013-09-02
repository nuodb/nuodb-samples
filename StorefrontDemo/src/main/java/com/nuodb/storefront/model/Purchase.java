/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotNull;

@Entity
public class Purchase extends Model {
    @ManyToOne
    @NotNull
    private Customer customer;

    @NotNull
    private Calendar datePurchased;

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy = "purchase")
    @OrderBy("dateAdded")
    private List<PurchaseSelection> selections = new ArrayList<PurchaseSelection>();

    public Purchase() {
    }

    public Customer getCustomer() {
        return customer;
    }

    void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Calendar getDatePurchased() {
        return datePurchased;
    }

    public void setDatePurchased(Calendar datePurchased) {
        this.datePurchased = datePurchased;
    }

    public List<PurchaseSelection> getSelections() {
        return selections;
    }

    public void addTransactionSelection(PurchaseSelection selection) {
        selection.setPurchase(this);
        selections.add(selection);
    }
}
