/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

public class CollectionUtil {
    private CollectionUtil() {
    }

    public static Collection<String> createCollectionWithNonEmptyItems(Collection<String> source) {
        Collection<String> items = null;
        if (source != null && !source.isEmpty()) {
            for (String item : source) {
                if (item != null && !item.isEmpty()) {
                    if (items == null) {
                        items = new ArrayList<String>();
                    }
                    items.add(item);
                }
            }
        }
        return items;
    }

    public static Collection<String> removeEmptyItems(Collection<String> list) {
        if (list != null) {
            for (Iterator<String> iter = list.iterator(); iter.hasNext();) {
                String item = iter.next();
                if (item == null || item.isEmpty()) {
                    iter.remove();
                }
            }
        }
        return list;
    }
}
