package com.github.onsdigital.session.service.client.client;

import com.github.onsdigital.session.service.client.error.SessionClientException;
import org.apache.http.client.methods.CloseableHttpResponse;

@FunctionalInterface
public interface ResponseHandler<T> {

    T handleResponse(CloseableHttpResponse response) throws SessionClientException;
}
