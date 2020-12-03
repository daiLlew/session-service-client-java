package com.session.service.client;

import com.session.service.Session;
import com.session.service.ZebedeeSession;
import com.session.service.entities.CreateNewSession;
import com.session.service.entities.SessionCreated;
import com.session.service.entities.SimpleMessage;
import com.session.service.error.SessionClientException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
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

    public SessionClientImpl(final String host, final String serviceAuthToken) {
        this.host = host;
        this.serviceAuthToken = serviceAuthToken;
        this.http = new Http();
    }

    public SessionClientImpl(String host, String serviceAuthToken, Http http) {
        this.host = host;
        this.serviceAuthToken = serviceAuthToken;
        this.http = http;
    }

    @Override
    public SessionCreated createNewSession(String userEmail) throws SessionClientException {
        SessionCreated sessionCreated = null;

        if (StringUtils.isNotEmpty(userEmail)) {
            sessionCreated = postSession(userEmail);
        }

        return sessionCreated;
    }

    private SessionCreated postSession(String userEmail) {
        try {
            return http.post(host, "/session", new CreateNewSession(userEmail), createSessionResponseHandler());
        } catch (Exception ex) {
            throw new SessionClientException(ex);
        }
    }

    ResponseHandler<SessionCreated> createSessionResponseHandler() {
        return (response -> {
            int status = response.getStatusLine().getStatusCode();

            if (status != HttpStatus.SC_CREATED) {
                String msg = format("create session returned incorrect status expected 201 but was {0}", status);
                throw new SessionClientException(msg);
            }

            return getResponseEntity(response, SessionCreated.class);
        });
    }


    @Override
    public Session getSessionByID(String sessionID) throws SessionClientException {
        ZebedeeSession session = null;

        if (StringUtils.isNotEmpty(sessionID)) {
            HttpGet httpGet = http.createHttpGet(host, "/session/" + sessionID);

            try {
                session = http.doGet(httpGet, getSessionResponseHandler());
            } catch (IOException ex) {
                throw new SessionClientException("TODO", ex);
            }
        }

        return session;
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
    public Session getSessionByEmail(String email) throws SessionClientException {
        ZebedeeSession session = null;

        if (StringUtils.isNotEmpty(email)) {
            String uri = format("/search?email={0}", email);
            HttpGet httpGet = http.createHttpGet(host, uri);

            try {
                session = http.doGet(httpGet, getSessionResponseHandler());
            } catch (IOException ex) {
                throw new SessionClientException("error executing get session by email request", ex);
            }
        }
        return session;
    }

    @Override
    public boolean sessionExists(String sessionID) throws SessionClientException {
        return getSessionByID(sessionID) != null;
    }

    @Override
    public boolean clear() throws SessionClientException {
        try {
            HttpDelete httpDelete = http.createHttpDelete(host, "/sessions");
            SimpleMessage body = http.doDelete(httpDelete, flushSessionResponseHandler());
            LOG.debug(body.getMessage());
        } catch (IOException ex) {
            throw new SessionClientException("error executing HTTPDelete request", ex);
        }
        return true;
    }

    ResponseHandler<SimpleMessage> flushSessionResponseHandler() {
        return (response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status != HttpStatus.SC_OK) {
                String msg = format("incorrect http status code expected 200 actual {0}", status);
                throw new SessionClientException(msg);
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
}
