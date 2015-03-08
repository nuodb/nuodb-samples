/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.model.dto;

public enum WorkloadStep {
    BROWSE,

    BROWSE_NEXT_PAGE,

    BROWSE_SEARCH,

    BROWSE_CATEGORY,

    BROWSE_SORT,

    PRODUCT_VIEW_DETAILS,

    PRODUCT_ADD_TO_CART,

    PRODUCT_ADD_REVIEW,

    CART_VIEW,

    CART_UPDATE,

    CART_CHECKOUT,

    ADMIN_RUN_REPORT,

    @WorkloadFlow(steps = {
            WorkloadStep.BROWSE,
            WorkloadStep.BROWSE_SORT,
            WorkloadStep.BROWSE_NEXT_PAGE,
            WorkloadStep.BROWSE_SEARCH,
            WorkloadStep.BROWSE_CATEGORY,
            WorkloadStep.PRODUCT_VIEW_DETAILS })
    MULTI_BROWSE,

    @WorkloadFlow(steps = {
            WorkloadStep.BROWSE,
            WorkloadStep.BROWSE_NEXT_PAGE,
            WorkloadStep.PRODUCT_VIEW_DETAILS,
            WorkloadStep.PRODUCT_ADD_TO_CART,
            WorkloadStep.BROWSE_SEARCH,
            WorkloadStep.PRODUCT_VIEW_DETAILS,
            WorkloadStep.PRODUCT_ADD_TO_CART,
            WorkloadStep.BROWSE_CATEGORY,
            WorkloadStep.BROWSE_NEXT_PAGE,
            WorkloadStep.PRODUCT_VIEW_DETAILS,
            WorkloadStep.PRODUCT_ADD_TO_CART,
            WorkloadStep.CART_VIEW,
            WorkloadStep.CART_UPDATE,
            WorkloadStep.CART_CHECKOUT })
    MULTI_SHOP,

    @WorkloadFlow(steps = { WorkloadStep.MULTI_BROWSE, WorkloadStep.PRODUCT_ADD_REVIEW })
    MULTI_BROWSE_AND_REVIEW;
}
