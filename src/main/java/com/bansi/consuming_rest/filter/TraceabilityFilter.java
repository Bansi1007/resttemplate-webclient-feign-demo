package com.bansi.consuming_rest.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public class TraceabilityFilter extends OncePerRequestFilter {

        public static final String HEADER_NAME = "X-Traceability-Id";
        public static final String MDC_KEY = "traceabilityId";

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
            String traceId = request.getHeader(HEADER_NAME);
            if (traceId == null || traceId.isBlank()) {
                traceId = UUID.randomUUID().toString();
            }
            try {
                MDC.put(MDC_KEY, traceId);
                response.setHeader(HEADER_NAME, traceId);
                filterChain.doFilter(request, response);
            } finally {
                MDC.remove(MDC_KEY); // always clean up, threads are pooled
            }
        }
    }

