package com.nuodb.storefront.model;

import java.util.Calendar;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotNull;

@Entity
public class Transaction extends Model {
    @ManyToOne
    @JoinColumn(name = "customer_id")
    @NotNull
    private Customer customer;

    @NotNull
    private Calendar datePurchased;

    @OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    @JoinColumn(name = "transaction_id")
    @OrderBy("dateAdded")
    private List<TransactionSelection> selections;

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
