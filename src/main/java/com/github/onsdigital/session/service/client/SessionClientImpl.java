package com.github.onsdigital.session.service.client;

import com.github.onsdigital.session.service.Session;
import com.github.onsdigital.session.service.ZebedeeSession;
import com.github.onsdigital.session.service.entities.SimpleMessage;
import com.github.onsdigital.session.service.error.SessionClientException;
import com.github.onsdigital.session.service.entities.CreateNewSession;
import com.github.onsdigital.session.service.entities.SessionCreated;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;

import static java.text.MessageFormat.format;

public class SessionClientImpl implements SessionClient {

    private static Logger LOG = LoggerFactory.getLogger(SessionClientImpl.class);

    private String host;
    private String serviceAuthToken;
    private Http http;

    public SessionClientImpl(final String host, final String serviceAuthToken, Http http) {
        this.host = host;
        this.serviceAuthToken = serviceAuthToken;
        this.http = http;
    }

    @Override
    public SessionCreated createNewSession(String userEmail) throws SessionClientException {
        SessionCreated sessionCreated;

        if (StringUtils.isNotEmpty(userEmail)) {
            sessionCreated = postSession(userEmail);
        } else {
            throw new SessionClientException("user email cannot be empty");
        }

        return sessionCreated;
    }

    private SessionCreated postSession(String userEmail) {
        try {
            return http.post(host, "/sessions", new CreateNewSession(userEmail), createSessionResponseHandler(), serviceAuthToken);
        } catch (Exception ex) {
            throw new SessionClientException(ex);
        }
    }

    ResponseHandler<SessionCreated> createSessionResponseHandler() {
        return (response -> {
            int status = response.getStatusLine().getStatusCode();

            if (status != HttpStatus.SC_CREATED) {
                throw new SessionClientException(format("create session returned incorrect status, expected 201 but was {0}", status));
            }

            return getResponseEntity(response, SessionCreated.class);
        });
    }


    @Override
    public Session getSessionByID(String sessionID) throws SessionClientException {
        return getSession(sessionID);
    }

    @Override
    public Session getSessionByEmail(String email) throws SessionClientException {
        return getSession(email);
    }

    ResponseHandler<ZebedeeSession> getSessionResponseHandler() {
        return (response -> {
            ZebedeeSession session = null;
            int statusCode = response.getStatusLine().getStatusCode();

            switch (statusCode) {
                case HttpStatus.SC_NOT_FOUND:
                    LOG.info("no session found for id");
                    break;
                case HttpStatus.SC_OK:
                    LOG.info("found session for id");
                    session = getResponseEntity(response, ZebedeeSession.class);
                    break;
                default:
                    String error = format("incorrect http status code expected {0} actual {1}",
                            HttpStatus.SC_OK, statusCode);
                    throw new SessionClientException(error);
            }
            return session;
        });
    }

    @Override
    public boolean sessionExists(String sessionID) throws SessionClientException {
        return getSessionByID(sessionID) != null;
    }

    @Override
    public boolean clear() throws SessionClientException {
        try {
            http.delete(host, "/sessions", flushSessionResponseHandler(), serviceAuthToken);
        } catch (IOException ex) {
            throw new SessionClientException("error executing HTTPDelete request", ex);
        }
        return true;
    }

    ResponseHandler<SimpleMessage> flushSessionResponseHandler() {
        return (response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status != HttpStatus.SC_OK) {
                throw new SessionClientException(format("incorrect http status code expected 200 actual {0}", status));
            }

            return getResponseEntity(response, SimpleMessage.class);
        });
    }

    public <T> T getResponseEntity(CloseableHttpResponse response, Class<T> tClass) throws SessionClientException {
        try (InputStreamReader reader = new InputStreamReader(response.getEntity().getContent())) {
            return http.fromJson(reader, tClass);
        } catch (Exception ex) {
            throw new SessionClientException("error reading content from http response", ex);
        }
    }

    private Session getSession(String sessionIdentifier) {
        if (StringUtils.isEmpty(sessionIdentifier)) {
            throw new SessionClientException("sessionIdentifier expected but is null");
        }

        ZebedeeSession session;
        try {
            session = http.get(host, "/sessions/" + sessionIdentifier, getSessionResponseHandler());
        } catch (IOException ex) {
            throw new SessionClientException("unable to retrieve session", ex);
        }

        if (session == null) {
            throw new SessionClientException("session not found");
        }

        return session;
    }
}
