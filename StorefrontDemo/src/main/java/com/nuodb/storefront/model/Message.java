/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.model;

public class Message {
    private MessageSeverity severity;
    private String message;
    private String[] buttons;

    public Message() {
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
