/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class Model implements IModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    public Model() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
