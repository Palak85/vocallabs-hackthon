package com.raj.document_qna_assistant.config;

public final class TenantContext {
    private static final String DEFAULT_TENANT = "default";
    private static final ThreadLocal<String> CURRENT_TENANT = new ThreadLocal<>();

    private TenantContext() {}

    public static String getCurrentTenant() {
        String tenant = CURRENT_TENANT.get();
        return (tenant != null && !tenant.isBlank()) ? tenant : DEFAULT_TENANT;
    }

    public static void setCurrentTenant(String tenantId) {
        CURRENT_TENANT.set(tenantId != null && !tenantId.isBlank() ? tenantId : DEFAULT_TENANT);
    }

    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
