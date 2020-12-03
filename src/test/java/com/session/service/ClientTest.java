package com.session.service;

import com.session.service.client.Http;
import com.session.service.client.ResponseHandler;
import com.session.service.client.SessionClient;
import com.session.service.client.SessionClientImpl;
import com.session.service.entities.SessionCreated;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@RunWith(MockitoJUnitRunner.class)
public class ClientTest {

    static final String EMAIL = "test@test.com";
    static final int SESSION_TIMEOUT_MS = 30000;

    private SessionClient client;

    @Mock
    private Http http;

    @Before
    public void setUp() throws Exception {
        final String host = "http://localhost:6666";
        client = new SessionClientImpl(host, "", http);

        final String returnedUri = "uri";
        final String sessionID = "sessionID";
        Mockito.when(http.post(eq(host), eq("/session"), any(Object.class), ArgumentMatchers.<ResponseHandler<SessionCreated>>any()))
                .thenReturn(new SessionCreated(returnedUri, sessionID));
    }

    @Test
    public void createSession_shouldCreateNewSession() {
        SessionCreated sessionCreated = client.createNewSession(EMAIL);
        assertThat(sessionCreated, is(notNullValue()));
        assertThat(sessionCreated.getId(), not(isEmptyString()));
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
