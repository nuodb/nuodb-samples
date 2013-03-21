package com.nuodb.storefront.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "Transaction_Selection")
public class TransactionSelection extends ProductSelection implements
        Serializable {
    private static final long serialVersionUID = 4243302747488634606L;

    @ManyToOne
    @NotNull
    @Id
    private Transaction transaction;

    public TransactionSelection() {
    }

    public TransactionSelection(ProductSelection selection) {
        super(selection);
    }

    public Transaction getTransaction() {
        return transaction;
    }

    void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}
