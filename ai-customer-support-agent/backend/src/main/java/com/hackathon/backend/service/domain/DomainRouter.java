package com.hackathon.backend.service.domain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class DomainRouter {

    private final Map<String, DomainHandler> handlerMap = new ConcurrentHashMap<>();
    private final GeneralQueryHandler fallbackHandler;

    public DomainRouter(List<DomainHandler> handlers, GeneralQueryHandler fallbackHandler) {
        this.fallbackHandler = fallbackHandler;
        for (DomainHandler handler : handlers) {
            if (handler.getDomain() != null) {
                handlerMap.put(handler.getDomain().toLowerCase(), handler);
            }
        }
        log.info("DomainRouter initialized with {} domain handlers: {}", handlerMap.size(), handlerMap.keySet());
    }

    public DomainHandler route(String domain) {
        if (domain == null || domain.isBlank()) {
            return fallbackHandler;
        }

        DomainHandler handler = handlerMap.get(domain.toLowerCase());
        if (handler != null) {
            return handler;
        }

        return fallbackHandler;
    }
}
