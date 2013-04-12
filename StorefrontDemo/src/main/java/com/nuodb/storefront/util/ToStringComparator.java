/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.util;

import java.util.Comparator;

public class ToStringComparator {
    private ToStringComparator() {
    }

    @SuppressWarnings("rawtypes")
    private static final Comparator s_comparator = new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
            return o1.toString().compareTo(o2.toString());
        }
    };

    @SuppressWarnings("unchecked")
    public static final <T> Comparator<T> getComparator() {
        return s_comparator;
    }
}
