package com.github.onsdigital.session.service.entities;

public class SessionCreated {

    private String uri;
    private String id;

    public SessionCreated(final String uri, final String id) {
        this.uri = uri;
        this.id = id;
    }

    public String getUri() {
        return this.uri;
    }

    public void setUri(final String uri) {
        this.uri = uri;
    }

    public String getId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }
}
