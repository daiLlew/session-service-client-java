package com.session.service.client;

import com.google.gson.Gson;
import com.session.service.Session;
import com.session.service.ZebedeeSession;
import com.session.service.entities.CreateNewSession;
import com.session.service.entities.SessionCreated;
import com.session.service.error.SessionClientException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStreamReader;

import static java.text.MessageFormat.format;

public class SessionClientImpl implements SessionClient {

    private static final String ACCEPT_HEADER_NAME = "Accept";
    private static final String CONTENT_TYPE_HEADER = "Content-type";
    private static final String APPLICATION_JSON = "application/json";

    private static SessionClientImpl INSTANCE = null;

    private String host;
    private String serviceAuthToken;
    private Gson gson;

    public SessionClientImpl(final String host, final String serviceAuthToken) {
        this.host = host;
        this.serviceAuthToken = serviceAuthToken;
        this.gson = new Gson();
    }

    @Override
    public SessionCreated createNewSession(String userEmail) throws SessionClientException {
        SessionCreated sessionCreated = null;

        if (StringUtils.isNotEmpty(userEmail)) {
            String jsonStr = gson.toJson(new CreateNewSession(userEmail));
            HttpPost httpPost = createHttpPost("/session", jsonStr);

            sessionCreated = doPost(httpPost, SessionCreated.class, HttpStatus.SC_CREATED);
        }
        return sessionCreated;
    }

    @Override
    public Session getSessionByID(String sessionID) throws SessionClientException {
        ZebedeeSession session = null;

        if (StringUtils.isNotEmpty(sessionID)) {
            HttpGet httpGet = createHttpGet("/session/" + sessionID);

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(httpGet)) {

                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == HttpStatus.SC_OK) {
                    session = getResponseEntity(response, ZebedeeSession.class);
                } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                    session = null;
                } else {
                    String error = format("incorrect http status code expected {0} actual {1}", HttpStatus.SC_OK, statusCode);
                    throw new SessionClientException(error);
                }

            } catch (IOException ex) {
                throw new SessionClientException("error executing HTTPGet request", ex);
            }
        }

        return session;
    }

    @Override
    public Session getSessionByEmail(String email) throws SessionClientException {
        ZebedeeSession session = null;

        if (StringUtils.isNotEmpty(email)) {
            String uri = format("/search?email={0}", email);
            HttpGet httpGet = createHttpGet(uri);

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(httpGet)) {

                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == HttpStatus.SC_OK) {
                    session = getResponseEntity(response, ZebedeeSession.class);
                } else if (statusCode == HttpStatus.SC_NOT_FOUND) {
                    session = null;
                } else {
                    String error = format("incorrect http status code expected {0} actual {1}", HttpStatus.SC_OK, statusCode);
                    throw new SessionClientException(error);
                }

            } catch (IOException ex) {
                throw new SessionClientException("error executing HTTPGet request", ex);
            }
        }
        return session;
    }

    <T> T doPost(HttpPost httpPost, Class<T> tClass, int expectedStatus) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(httpPost)) {

            checkResponseStatusCode(HttpStatus.SC_CREATED, response);

            return getResponseEntity(response, tClass);
        } catch (IOException ex) {
            throw new SessionClientException("error executing HTTPPost request", ex);
        }
    }

    HttpPost createHttpPost(String uri, String json) {
        try {
            HttpPost httpPost = new HttpPost(host + uri);
            httpPost.setEntity(new StringEntity(json));
            httpPost.setHeader(ACCEPT_HEADER_NAME, APPLICATION_JSON);
            httpPost.setHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON);
            return httpPost;
        } catch (Exception ex) {
            throw new SessionClientException("error creating HttpPost", ex);
        }
    }

    HttpGet createHttpGet(String uri) {
        HttpGet httpGet = new HttpGet(host + uri);
        httpGet.setHeader(ACCEPT_HEADER_NAME, APPLICATION_JSON);
        httpGet.setHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON);
        return httpGet;
    }

    void checkResponseStatusCode(int expected, CloseableHttpResponse response) {
        int actual = response.getStatusLine().getStatusCode();
        if (expected != actual) {
            String error = format("incorrect http status code expected {0} actual {1}", expected, actual);
            throw new SessionClientException(error);
        }
    }

    <T> T getResponseEntity(CloseableHttpResponse response, Class<T> tClass) {
        try (InputStreamReader reader = new InputStreamReader(response.getEntity().getContent())) {
            return new Gson().fromJson(reader, tClass);
        } catch (Exception ex) {
            String error = format("error reading content from http response", ex);
            throw new SessionClientException(error, ex);
        }
    }
}
