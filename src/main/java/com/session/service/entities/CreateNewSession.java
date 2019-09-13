package com.session.service.entities;

public class CreateNewSession {

    private String email;

    public CreateNewSession(final String email) {
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }
}
