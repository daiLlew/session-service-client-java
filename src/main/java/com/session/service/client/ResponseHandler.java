package com.session.service.client;

import com.session.service.error.SessionClientException;
import org.apache.http.client.methods.CloseableHttpResponse;

@FunctionalInterface
public interface ResponseHandler<T> {

    T handleResponse(CloseableHttpResponse response) throws SessionClientException;
}
