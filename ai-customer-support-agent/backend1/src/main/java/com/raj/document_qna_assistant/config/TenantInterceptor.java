package com.raj.document_qna_assistant.config;

import com.raj.document_qna_assistant.repository.TenantRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.util.StringUtils;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    public static final String TENANT_HEADER = "X-Tenant-Id";
    public static final String DEFAULT_TENANT = "default";
    private final TenantRepository tenantRepository;

    public TenantInterceptor(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Bypass actuator endpoints and pre-flight CORS requests
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/actuator") || "OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String tenantId = request.getHeader(TENANT_HEADER);
        if (!StringUtils.hasText(tenantId)) {
            tenantId = DEFAULT_TENANT;
        }

        // Auto-register tenant in repository if it doesn't exist yet
        try {
            if (!tenantRepository.existsById(tenantId)) {
                tenantRepository.save(tenantId, "Tenant " + tenantId);
            }
        } catch (Exception ignored) {
            // Safe fallback if database is bootstrapping
        }

        TenantContext.setCurrentTenant(tenantId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        TenantContext.clear();
    }
}
