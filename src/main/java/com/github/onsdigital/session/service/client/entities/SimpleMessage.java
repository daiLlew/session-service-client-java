package com.github.onsdigital.session.service.client.entities;

public class SimpleMessage {

    private String message;

    public SimpleMessage(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(final String message) {
        this.message = message;
    }
}
