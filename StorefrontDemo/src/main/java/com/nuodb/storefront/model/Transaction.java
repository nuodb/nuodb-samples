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
public class Transaction extends Model {
    @ManyToOne
    @NotNull
    private Customer customer;

    @NotNull
    private Calendar datePurchased;

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER, orphanRemoval = true, mappedBy="transaction")
    @OrderBy("dateAdded")
    private List<TransactionSelection> selections = new ArrayList<TransactionSelection>();

    public Transaction() {
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

    public List<TransactionSelection> getSelections() {
        return selections;
    }

    public void addTransactionSelection(TransactionSelection selection) {
        selection.setTransaction(this);
        selections.add(selection);
    }
}
