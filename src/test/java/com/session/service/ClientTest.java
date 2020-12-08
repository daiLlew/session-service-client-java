package com.session.service;

import com.session.service.client.Http;
import com.session.service.client.ResponseHandler;
import com.session.service.client.SessionClient;
import com.session.service.client.SessionClientImpl;
import com.session.service.entities.SessionCreated;
import com.session.service.error.SessionClientException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class ClientTest {

    static final String EMAIL = "test@test.com";
    static final int SESSION_TIMEOUT_MS = 30000;
    static final String HOST = "http://localhost:24400";
    static final String RETURNED_URI = "uri";
    static final String SESSION_ID = "sessionsID";

    private SessionClient client;
    private ZebedeeSession zebedeeSession;

    @Mock
    private Http http;

    @Before
    public void setUp() {
        client = new SessionClientImpl(HOST, "1234", http);

        zebedeeSession = new ZebedeeSession();
        zebedeeSession.setId(SESSION_ID);
        zebedeeSession.setEmail(EMAIL);
    }

    @Test
    public void createSession_shouldCreateNewSession() throws IOException {
        Mockito.when(http.post(
                eq(HOST),
                eq("/sessions"),
                any(Object.class),
                ArgumentMatchers.<ResponseHandler<SessionCreated>>any(),
                any(String.class)
        )).thenReturn(new SessionCreated(RETURNED_URI, SESSION_ID));

        SessionCreated sessionCreated = client.createNewSession(EMAIL);
        
        assertThat(sessionCreated, is(notNullValue()));
        assertThat(sessionCreated.getId(), is(SESSION_ID));
    }

    @Test
    public void createSession_emptyEmailAddress_shouldReturnError() {
        SessionClientException sessionClientException = assertThrows(SessionClientException.class, () ->
                client.createNewSession(null));

        assertThat(sessionClientException.getMessage(), is("user email cannot be empty"));
    }

    @Test
    public void getSessionById_shouldReturnExpectedSession() throws IOException {
        Mockito.when(http.get(
                eq(HOST),
                eq("/sessions/" + SESSION_ID),
                ArgumentMatchers.<ResponseHandler<Session>>any()
        )).thenReturn(zebedeeSession);

        Session session = client.getSessionByID(SESSION_ID);

        assertThat(session, is(notNullValue()));
        assertThat(session.getId(), equalTo(SESSION_ID));
    }

    @Test
    public void getSessionById_whenNullSessionReturned_shouldReturnError() throws IOException {
        Mockito.when(http.get(
                eq(HOST),
                eq("/sessions/" + SESSION_ID),
                ArgumentMatchers.<ResponseHandler<Session>>any()
        )).thenReturn(null);

        SessionClientException sessionClientException = assertThrows(SessionClientException.class, () ->
                client.getSessionByID(SESSION_ID));

        assertThat(sessionClientException.getMessage(), is("session not found"));
    }

    @Test
    public void getSessionById_httpThrowsIOException_shouldReturnError() throws IOException {
        Mockito.when(http.get(
                eq(HOST),
                eq("/sessions/" + SESSION_ID),
                ArgumentMatchers.<ResponseHandler<Session>>any()
        )).thenThrow(new IOException());

        SessionClientException sessionClientException = assertThrows(SessionClientException.class, () ->
                client.getSessionByID(SESSION_ID));

        assertThat(sessionClientException.getMessage(), not(nullValue()));
    }

    @Test
    public void getSessionByEmail_shouldReturnExpectedSession() throws Exception {
        Mockito.when(http.get(
                eq(HOST),
                eq("/sessions/" + EMAIL),
                ArgumentMatchers.<ResponseHandler<Session>>any()
        )).thenReturn(zebedeeSession);

        Session session = client.getSessionByEmail(EMAIL);

        assertThat(session, is(notNullValue()));
        assertThat(session.getId(), is(SESSION_ID));
    }

    @Test
    public void getSessionByEmail_whenNullSessionReturned_shouldReturnError() throws IOException {
        Mockito.when(http.get(
                eq(HOST),
                eq("/sessions/" + EMAIL),
                ArgumentMatchers.<ResponseHandler<Session>>any()
        )).thenReturn(null);

        SessionClientException sessionClientException = assertThrows(SessionClientException.class, () ->
                client.getSessionByEmail(EMAIL));

        assertThat(sessionClientException.getMessage(), is("session not found"));
    }

    @Test
    public void getSessionByEmail_httpThrowsIOException_shouldReturnError() throws IOException {
        Mockito.when(http.get(
                eq(HOST),
                eq("/sessions/" + EMAIL),
                ArgumentMatchers.<ResponseHandler<Session>>any()
        )).thenThrow(new IOException());

        SessionClientException sessionClientException = assertThrows(SessionClientException.class, () ->
                client.getSessionByEmail(EMAIL));

        assertThat(sessionClientException.getMessage(), not(nullValue()));
    }

    @Test
    public void clearSessions_shouldBeExpired() throws Exception {
        SessionCreated sessionCreated = client.createNewSession(EMAIL);
        assertThat(sessionCreated, is(notNullValue()));
        assertThat(sessionCreated.getId(), not(isEmptyString()));

        client.clear();

        assertThat(client.getSessionByEmail(EMAIL), is(nullValue()));
    }
}
