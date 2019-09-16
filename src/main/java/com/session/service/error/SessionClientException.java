package com.session.service.error;

public class SessionClientException extends RuntimeException {

    public SessionClientException() {
    }

    public SessionClientException(final String message) {
        super(message);
    }

    public SessionClientException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public SessionClientException(final Throwable cause) {
        super(cause);
    }
}
