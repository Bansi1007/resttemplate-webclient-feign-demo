package com.bansi.consuming_rest.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SuperheroAuthInterceptor implements ClientHttpRequestInterceptor {

    @Autowired
    private SuperheroTokenProvider tokenProvider;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        String token = tokenProvider.getToken();
        System.out.println("Attaching token to Superhero call: " + token);
        request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getToken());
        return execution.execute(request, body);
    }
}