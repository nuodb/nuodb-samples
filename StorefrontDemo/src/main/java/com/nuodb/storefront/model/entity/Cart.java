/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.model.entity;

import java.math.BigDecimal;

import com.googlecode.genericdao.search.SearchResult;

public class Cart extends SearchResult<CartSelection> {
    private static final long serialVersionUID = 712931974595245032L;

    private BigDecimal totalPrice;

    public Cart() {
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
}
