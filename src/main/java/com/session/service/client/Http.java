package com.session.service.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.session.service.json.SessionDateFormatter;
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

    private Gson gson;

    public Http() {
        this.gson = new GsonBuilder().registerTypeAdapter(Date.class, new SessionDateFormatter()).create();
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

    public <T> T doGet(HttpGet httpGet, ResponseHandler<T> responseHandler) throws IOException {
        try (
                CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(httpGet)
        ) {
            return responseHandler.handleResponse(response);
        }
    }

    public <T> T doPost(HttpPost httpPost, ResponseHandler<T> responseHandler) throws IOException {
        try (
                CloseableHttpClient httpClient = HttpClients.createDefault();
                CloseableHttpResponse response = httpClient.execute(httpPost)
        ) {
            return responseHandler.handleResponse(response);
        }
    }

    public <T> T doDelete(HttpDelete httpDelete, ResponseHandler<T> responseHandler) throws IOException {
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
