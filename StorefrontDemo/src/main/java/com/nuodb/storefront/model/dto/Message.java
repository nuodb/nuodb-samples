/* Copyright (c) 2013-2015 NuoDB, Inc. */

package com.nuodb.storefront.model.dto;

import com.nuodb.storefront.model.type.MessageSeverity;

public class Message {
    private MessageSeverity severity;
    private String message;
    private String[] buttons;
    private MessageLink[] links;

    public Message() {
    }

    public Message(Exception e) {
        this(MessageSeverity.ERROR, (e.getMessage() == null) ? e.getClass().getSimpleName() : e.getMessage());
    }

    public Message(MessageSeverity severity, String message, String... buttons) {
        this.severity = severity;
        this.message = message;
        this.buttons = buttons;
    }

    public MessageSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(MessageSeverity severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String[] getButtons() {
        return buttons;
    }

    public void setButtons(String[] buttons) {
        this.buttons = buttons;
    }

    public MessageLink[] getLinks() {
        return links;
    }

    public void setLinks(MessageLink[] links) {
        this.links = links;
    }
    
    public void setLink(MessageLink link) {
        if (link == null) {
            this.links = null;
        } else {
            this.links = new MessageLink[] { link };
        }
    }
}
