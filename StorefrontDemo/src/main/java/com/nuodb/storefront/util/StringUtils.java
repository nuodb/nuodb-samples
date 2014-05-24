package com.nuodb.storefront.util;

public class StringUtils {
    /**
     * Is the given string empty (size 0 or null)
     * 
     * @param string
     * @return if the string is empty (size 0 or null)
     */
    public static boolean isEmpty(CharSequence string) {
        return size(string) == 0;
    }

    /**
     * return the size of the given string in a null friendly fashion
     * 
     * @param string
     * @return size of the string or 0 if string is null
     */
    public static int size(CharSequence string) {
        return string == null ? 0 : string.length();
    }

    public static String toSentenceCase(String str) {
        if (isEmpty(str)) {
            return str;
        }
        if (str.length() == 1) {
            return str.toUpperCase();
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1).toLowerCase();
    }
}
