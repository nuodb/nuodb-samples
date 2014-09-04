/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.model.dto;

import java.util.Collection;

import com.nuodb.storefront.model.type.ProductSort;

public class ProductFilter extends PaginationFilter {
    private String matchText = null;
    private Collection<String> categories = null;
    private ProductSort sort = ProductSort.RELEVANCE;

    public ProductFilter() {
    }

    public ProductFilter(Integer page, Integer pageSize, String matchText, Collection<String> categories, ProductSort sort) {
        super(page, pageSize);
        this.matchText = matchText;
        this.categories = categories;
        this.sort = sort;
    }

    /**
     * Text that must appear in the name and/or description of the product (case insensitive). Ignored if null or empty. Leading and trailing space is
     * ignored.
     */
    public String getMatchText() {
        return matchText;
    }

    public void setMatchText(String matchText) {
        this.matchText = matchText;
    }

    /**
     * When non-null and non-empty, excludes products not in any of the categories specified (case sensitive).
     */
    public Collection<String> getCategories() {
        return categories;
    }

    public void setCategories(Collection<String> categories) {
        this.categories = categories;
    }
    
    /**
     * The order in which items should be returned. Leave null for an undefined ordering.
     */
    public ProductSort getSort() {
        return sort;
    }

    public void setSort(ProductSort sort) {
        this.sort = sort;
    }
}
