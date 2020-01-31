package com.session.service.client;

import com.session.service.error.SessionClientException;
import org.apache.http.client.methods.CloseableHttpResponse;

@FunctionalInterface
public interface StatusChecker {

    void check(CloseableHttpResponse response) throws SessionClientException;
}
