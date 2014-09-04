/* Copyright (c) 2013-2014 NuoDB, Inc. */

package com.nuodb.storefront.model.dto;

import com.googlecode.genericdao.search.SearchResult;

public class PaginationFilter {
    private Integer page = 1;
    private Integer pageSize = 30;

    public PaginationFilter() {
    }

    public PaginationFilter(Integer page, Integer pageSize) {
        this.page = page;
        this.pageSize = pageSize;
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
}
