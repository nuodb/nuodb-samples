/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.model.dto;

public class ProductReviewFilter extends PaginationFilter {
    private Long productId;

    public ProductReviewFilter() {
    }

    public ProductReviewFilter(Integer page, Integer pageSize, long productId) {
        super(page, pageSize);
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }
}
