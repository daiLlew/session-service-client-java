package com.session.service;

import com.google.gson.Gson;
import com.session.service.entities.CreateNewSession;
import com.session.service.entities.SessionCreated;
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

public class Client {

    private static Client INSTANCE = null;

    private String host;
    private String serviceAuthToken;
    private Gson gson;

    public Client(final String host, final String serviceAuthToken) {
        this.host = host;
        this.serviceAuthToken = serviceAuthToken;
        this.gson = new Gson();
    }

    public SessionCreated createNewSession(String userEmail) {
        SessionCreated sessionCreated = null;

        if (StringUtils.isNotEmpty(userEmail)) {
            String jsonStr = gson.toJson(new CreateNewSession(userEmail));
            HttpPost httpPost = createHttpPost("/session", jsonStr);

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(httpPost)) {

                checkResponseStatusCode(HttpStatus.SC_CREATED, response);
                sessionCreated = getResponseEntity(response, SessionCreated.class);
            } catch (IOException ex) {
                throw new RuntimeException("TODO", ex);
            }
        }
        return sessionCreated;
    }

    public Session getSessionByID(String sessionID) {
        SessionImpl session = null;

        if (StringUtils.isNotEmpty(sessionID)) {
            HttpGet httpGet = createHttpGet("/session/" + sessionID);

            try (CloseableHttpClient httpClient = HttpClients.createDefault();
                 CloseableHttpResponse response = httpClient.execute(httpGet)) {

                if (HttpStatus.SC_OK != response.getStatusLine().getStatusCode()) {
                    throw new RuntimeException("TODO 1");
                }

                session = getResponseEntity(response, SessionImpl.class);
            } catch (IOException ex) {
                throw new RuntimeException("TODO", ex);
            }
        }
        return session;
    }

    HttpPost createHttpPost(String uri, String json) {
        try {
            HttpPost httpPost = new HttpPost(host + uri);
            httpPost.setEntity(new StringEntity(json));
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            return httpPost;
        } catch (Exception ex) {
            throw new RuntimeException("TODO");
        }
    }

    HttpGet createHttpGet(String uri) {
        HttpGet httpGet = new HttpGet(host + uri);
        httpGet.setHeader("Accept", "application/json");
        httpGet.setHeader("Content-type", "application/json");
        return httpGet;
    }

    void checkResponseStatusCode(int expected, CloseableHttpResponse response) {
        if (expected != response.getStatusLine().getStatusCode()) {
            throw new RuntimeException("TODO 1");
        }
    }

    <T> T getResponseEntity(CloseableHttpResponse response, Class<T> tClass) {
        try (InputStreamReader reader = new InputStreamReader(response.getEntity().getContent())) {
            return new Gson().fromJson(reader, tClass);
        } catch (Exception ex) {
            throw new RuntimeException("TODO 3", ex);
        }
    }
}
