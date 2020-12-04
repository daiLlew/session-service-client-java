package com.session.service;

import com.session.service.client.Http;
import com.session.service.client.ResponseHandler;
import com.session.service.client.SessionClient;
import com.session.service.client.SessionClientImpl;
import com.session.service.entities.SessionCreated;
import com.session.service.error.SessionClientException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.junit.Assert.assertThrows;

@RunWith(MockitoJUnitRunner.class)
public class ClientTest {

    static final String EMAIL = "test@test.com";
    static final int SESSION_TIMEOUT_MS = 30000;
    static final String HOST = "http://localhost:24400";
    static final String RETURNED_URI = "uri";
    static final String SESSION_ID = "sessionsID";

    private SessionClient client;

    @Mock
    private Http http;

    @Before
    public void setUp() {
        client = new SessionClientImpl(HOST, "1234", http);
    }

    @Test
    public void createSession_shouldCreateNewSession() throws IOException {
        Mockito.when(http.post(
                eq(HOST),
                eq("/sessions"),
                any(Object.class),
                ArgumentMatchers.<ResponseHandler<SessionCreated>>any()
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
    public void getSessionById_shouldReturnExpectedSession() throws Exception {
        SessionCreated sessionCreated = client.createNewSession(EMAIL);
        assertThat(sessionCreated, is(notNullValue()));
        assertThat(sessionCreated.getId(), not(isEmptyString()));

        Session session = client.getSessionByID(sessionCreated.getId());
        assertThat(session, is(notNullValue()));
        assertThat(session.getId(), equalTo(sessionCreated.getId()));
    }

    @Test
    public void getSessionByEmail_shouldReturnExpectedSession() throws Exception {
        SessionCreated sessionCreated = client.createNewSession(EMAIL);
        assertThat(sessionCreated, is(notNullValue()));
        assertThat(sessionCreated.getId(), not(isEmptyString()));

        Session session = client.getSessionByEmail(EMAIL);
        assertThat(session, is(notNullValue()));
        assertThat(session.getId(), equalTo(sessionCreated.getId()));
    }

    @Test
    public void getSessionByID_timeoutExpired_shouldReturnNotFound() throws Exception {
        SessionCreated sessionCreated = client.createNewSession(EMAIL);

        System.out.println("waiting a bit...");
        Thread.sleep(SESSION_TIMEOUT_MS);
        System.out.println("wait ended...");

        assertThat(client.getSessionByID(sessionCreated.getId()), is(nullValue()));
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
