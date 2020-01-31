package com.session.service.client;

import com.google.gson.Gson;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class Http {

    private static final String ACCEPT_HEADER_NAME = "Accept";
    private static final String CONTENT_TYPE_HEADER = "Content-type";
    private static final String APPLICATION_JSON = "application/json";

    private Gson gson;

    public Http() {
        this.gson = new Gson();
    }

    public HttpGet createHttpGet(String host, String uri) {
        HttpGet httpGet = new HttpGet(host + uri);
        httpGet.setHeader(ACCEPT_HEADER_NAME, APPLICATION_JSON);
        httpGet.setHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON);
        return httpGet;
    }

    public HttpPost createHttpPost(String host, String uri, String json) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(host + uri);
        httpPost.setEntity(new StringEntity(json));
        httpPost.setHeader(ACCEPT_HEADER_NAME, APPLICATION_JSON);
        httpPost.setHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON);
        return httpPost;
    }

    public HttpDelete createHttpDelete(String host, String uri) {
        HttpDelete httpDelete = new HttpDelete(host + uri);
        httpDelete.setHeader(ACCEPT_HEADER_NAME, APPLICATION_JSON);
        httpDelete.setHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON);
        return httpDelete;
    }

    public <T> T doGet(HttpGet httpGet, Class<T> tClass, StatusChecker statusChecker) throws IOException {
        try (
                CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(httpGet)
        ) {
            statusChecker.check(response);
            return getResponseEntity(response, tClass);
        }
    }

    public <T> T doPost(HttpPost httpPost, Class<T> tClass, StatusChecker statusChecker) throws IOException {
        try (
                CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(httpPost)
        ) {
            statusChecker.check(response);
            return getResponseEntity(response, tClass);
        }
    }

    public <T> T doDelete(HttpDelete httpDelete, Class<T> tClass, StatusChecker statusChecker) throws IOException {
        try (
                CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(httpDelete)
        ) {
            statusChecker.check(response);
            return getResponseEntity(response, tClass);
        }
    }

    <T> T getResponseEntity(CloseableHttpResponse response, Class<T> tClass) throws IOException {
        try (InputStreamReader reader = new InputStreamReader(response.getEntity().getContent())) {
            return gson.fromJson(reader, tClass);
        } catch (Exception ex) {
            throw new IOException("error reading content from http response", ex);
        }
    }

}
