/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.model;

import java.util.Collection;

import com.googlecode.genericdao.search.SearchResult;
import com.nuodb.storefront.util.CollectionUtil;

public class ProductFilter {
    private String matchText = null;
    private Collection<String> categories = null;
    private Integer page = 1;
    private Integer pageSize = 30;
    private ProductSort sort = ProductSort.RELEVANCE;

    public ProductFilter() {
    }

    public ProductFilter(String matchText, Collection<String> categories, Integer page, Integer pageSize, ProductSort sort) {
        this.matchText = matchText;
        this.categories = CollectionUtil.createCollectionWithNonEmptyItems(categories);
        this.page = page;
        this.pageSize = pageSize;
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
     * When non-null, specifies the max number of items to return. If only a subset of products is returned, you can still determine the total number
     * of products matching the criteria by using the {@link SearchResult#getTotalCount()} property.
     */
    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    /**
     * When non-null, indicates the first item to return from the resultset (offset is page * pageSize), so the page parameter is also required.
     */
    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
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
