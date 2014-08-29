package com.nuodb.storefront.model.dto;

public class MessageLink {
    private final String text;
    private final String href;

    public MessageLink(String text, String href) {
        this.text = text;
        this.href = href;
    }

    public String getText() {
        return text;
    }

    public String getHref() {
        return href;
    }
}
