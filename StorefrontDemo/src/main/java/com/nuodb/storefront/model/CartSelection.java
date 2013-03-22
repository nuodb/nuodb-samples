package com.nuodb.storefront.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Cart_Selection")
public class CartSelection extends ProductSelection implements Serializable {
    private static final long serialVersionUID = 6424330274748863460L;

    @Id
    @ManyToOne
    private Customer customer;

    public CartSelection() {
    }

    public Customer getCustomer() {
        return customer;
    }

    void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public void clearCustomer() {
        this.customer = null;
    }
}
