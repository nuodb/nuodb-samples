/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.model.dto;

import com.nuodb.storefront.model.type.MessageSeverity;

public class Message {
    private MessageSeverity severity;
    private String message;
    private String[] buttons;

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
}
