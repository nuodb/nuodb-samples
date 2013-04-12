/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.model;

public class Message {
    private MessageSeverity severity;
    private String message;
    
    public Message() {
    }
    
    public Message(MessageSeverity severity, String message) {
        this.severity = severity;
        this.message = message;
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
}
