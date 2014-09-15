/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.model.dto;

public class ProductReviewFilter extends PaginationFilter {
    private Integer productId;

    public ProductReviewFilter() {
    }

    public ProductReviewFilter(Integer page, Integer pageSize, int productId) {
        super(page, pageSize);
        this.productId = productId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }
}
