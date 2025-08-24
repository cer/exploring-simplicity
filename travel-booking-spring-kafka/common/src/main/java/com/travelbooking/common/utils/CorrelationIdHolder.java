package com.travelbooking.common.utils;

import org.slf4j.MDC;

import java.util.UUID;

public final class CorrelationIdHolder {
    
    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final ThreadLocal<String> CORRELATION_ID = new ThreadLocal<>();
    
    private CorrelationIdHolder() {
        throw new UnsupportedOperationException("CorrelationIdHolder class should not be instantiated");
    }
    
    public static void setCorrelationId(String correlationId) {
        if (correlationId != null) {
            CORRELATION_ID.set(correlationId);
            MDC.put(CORRELATION_ID_KEY, correlationId);
        }
    }
    
    public static String getCorrelationId() {
        String correlationId = CORRELATION_ID.get();
        if (correlationId == null) {
            correlationId = generateCorrelationId();
            setCorrelationId(correlationId);
        }
        return correlationId;
    }
    
    public static String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }
    
    public static void clear() {
        CORRELATION_ID.remove();
        MDC.remove(CORRELATION_ID_KEY);
    }
}