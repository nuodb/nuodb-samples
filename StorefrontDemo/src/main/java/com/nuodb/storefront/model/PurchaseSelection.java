package com.nuodb.storefront.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "Purchase_Selection")
public class PurchaseSelection extends ProductSelection implements Serializable {
    private static final long serialVersionUID = 4243302747488634606L;

    @Id
    @ManyToOne
    private Purchase purchase;

    public PurchaseSelection() {
    }

    public PurchaseSelection(ProductSelection selection) {
        super(selection);
    }

    public Purchase getPurchase() {
        return purchase;
    }

    void setPurchase(Purchase purchase) {
        this.purchase = purchase;
    }
}
