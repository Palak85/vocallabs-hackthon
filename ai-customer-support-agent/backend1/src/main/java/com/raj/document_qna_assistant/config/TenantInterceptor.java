package com.raj.document_qna_assistant.config;

import com.raj.document_qna_assistant.repository.TenantRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.util.StringUtils;

@Component
public class TenantInterceptor implements HandlerInterceptor {

    private static final String TENANT_HEADER = "X-Tenant-Id";
    private final TenantRepository tenantRepository;

    public TenantInterceptor(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Bypass actuator endpoints
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/actuator")) {
            return true;
        }

        String tenantId = request.getHeader(TENANT_HEADER);
        if (!StringUtils.hasText(tenantId)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing required request header: X-Tenant-Id\"}");
            return false;
        }

        // Auto-register tenant if it doesn't exist yet
        if (!tenantRepository.existsById(tenantId)) {
            tenantRepository.save(tenantId, "Tenant " + tenantId);
        }

        TenantContext.setCurrentTenant(tenantId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        TenantContext.clear();
    }
}
