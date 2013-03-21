package com.nuodb.storefront.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotNull;

@Entity
public class Customer extends Model {
    @OneToMany(cascade = { CascadeType.ALL })
    @JoinColumn(name = "customer_id")
    @OrderBy("dateAdded")
    private List<CartSelection> cartSelections = new ArrayList<CartSelection>();

    @OneToMany(cascade = { CascadeType.ALL })
    @JoinColumn(name = "customer_id")
    @OrderBy("datePurchased")
    private List<Transaction> transactions = new ArrayList<Transaction>();

    public String emailAddress;

    @NotNull
    private Calendar dateAdded;

    @NotNull
    private Calendar dateLastActive;

    public Customer() {
    }

    public List<CartSelection> getCartSelections() {
        return cartSelections;
    }

    public List<Transaction> getTransactions() {
        return transactions;
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
    
    public void addTransaction(Transaction transaction) {
        transaction.setCustomer(this);
        transactions.add(transaction);
    }
    
    public void clearTransactions() {
        transactions = null;
    }
    
    public void clearCartSelections() {
        cartSelections = null;
    }
}
