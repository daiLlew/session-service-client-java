package com.session.service.client;

import com.session.service.Session;
import com.session.service.entities.SessionCreated;
import com.session.service.error.SessionClientException;

/**
 * Defines a HTTP client for communicating with the sessions service API.
 */
public interface SessionClient {

    /**
     * Create a new {@link Session}.
     *
     * @param userEmail the email of the user to create a session for.
     * @return a new {@link Session}
     * @throws SessionClientException for any errors while attempting to create the session.
     */
    SessionCreated createNewSession(String userEmail) throws SessionClientException;

    /**
     * Get a {@link Session} by its ID.
     *
     * @param sessionID the ID of the session to get.
     * @return the {@link Session} with the requested ID if it exists, returns null if the session does not exist.
     * @throws SessionClientException for any errors while attempting to get the session.
     */
    Session getSessionByID(String sessionID) throws SessionClientException;

    /**
     * Get a {@link Session} by the user email it belongs to.
     *
     * @param email the email to search for.
     * @return the {@link Session} with the requested ID if it exists, returns null if the session does not exist.
     * @throws SessionClientException for any errors while attempting to get the session.
     */
    Session getSessionByEmail(String email) throws SessionClientException;

    /**
     * Check if a {@link Session} with the provided ID exists.
     *
     * @param sessionID the ID of the session to look for.
     * @return true if the session exists, false if not.
     * @throws SessionClientException for any errors while attempting to check the session.
     */
    boolean sessionExists(String sessionID) throws SessionClientException;

    boolean clear() throws SessionClientException;
}
