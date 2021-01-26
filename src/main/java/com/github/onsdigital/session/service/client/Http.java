package com.github.onsdigital.session.service.client;

import com.github.onsdigital.session.service.json.SessionDateFormatter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Date;

public class Http {

    private static final String ACCEPT_HEADER_NAME = "Accept";
    private static final String CONTENT_TYPE_HEADER = "Content-type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String AUTHORIZATION = "Authorization";

    private Gson gson;

    public Http() {
        this.gson = new GsonBuilder().registerTypeAdapter(Date.class, new SessionDateFormatter()).create();
    }

    public <T> T post(String host, String uri, Object obj, ResponseHandler<T> responseHandler, String serviceAuthToken) throws IOException {
        String jsonStr = toJson(obj);
        HttpPost httpPost = createHttpPost(host, uri, jsonStr, serviceAuthToken);
        return doPost(httpPost, responseHandler);
    }

    public <T> T get(String host, String uri, ResponseHandler<T> responseHandler) throws IOException {
        HttpGet httpGet = createHttpGet(host, uri);
        return doGet(httpGet, responseHandler);
    }

    public <T> T delete(String host, String uri, ResponseHandler<T> responseHandler, String serviceAuthToken) throws IOException {
        HttpDelete httpDelete = createHttpDelete(host, uri, serviceAuthToken);
        return doDelete(httpDelete, responseHandler);
    }

    private HttpGet createHttpGet(String host, String uri) {
        HttpGet httpGet = new HttpGet(host + uri);
        httpGet.setHeader(ACCEPT_HEADER_NAME, APPLICATION_JSON);
        httpGet.setHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON);
        return httpGet;
    }

    private HttpPost createHttpPost(String host, String uri, String json, String serviceAuthToken) throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(host + uri);
        httpPost.setEntity(new StringEntity(json));
        httpPost.setHeader(ACCEPT_HEADER_NAME, APPLICATION_JSON);
        httpPost.setHeader(CONTENT_TYPE_HEADER, APPLICATION_JSON);
        httpPost.setHeader(AUTHORIZATION, "Bearer " + serviceAuthToken);
        return httpPost;
    }

    private HttpDelete createHttpDelete(String host, String uri, String serviceAuthToken) {
        HttpDelete httpDelete = new HttpDelete(host + uri);
        httpDelete.setHeader(ACCEPT_HEADER_NAME, APPLICATION_JSON);
        httpDelete.setHeader(AUTHORIZATION, "Bearer " + serviceAuthToken);
        return httpDelete;
    }

    private <T> T doGet(HttpGet httpGet, ResponseHandler<T> responseHandler) throws IOException {
        try (
                CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(httpGet)
        ) {
            return responseHandler.handleResponse(response);
        }
    }

    private <T> T doPost(HttpPost httpPost, ResponseHandler<T> responseHandler) throws IOException {
        try (
                CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(httpPost)
        ) {
            return responseHandler.handleResponse(response);
        }
    }

    private <T> T doDelete(HttpDelete httpDelete, ResponseHandler<T> responseHandler) throws IOException {
        try (
                CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(httpDelete)
        ) {
            return responseHandler.handleResponse(response);
        }
    }

    public String toJson(Object obj) {
        return this.gson.toJson(obj);
    }

    public <T> T fromJson(Reader reader, Class<T> tClass) {
        return gson.fromJson(reader, tClass);
    }

}
