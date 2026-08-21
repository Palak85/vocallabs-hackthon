package com.hackathon.backend.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefusalResult {

    private boolean allowed;
    private String reason;
    private String refusalResponse;

    public static RefusalResult allow() {
        return RefusalResult.builder()
                .allowed(true)
                .build();
    }

    public static RefusalResult refuse(String reason, String refusalResponse) {
        return RefusalResult.builder()
                .allowed(false)
                .reason(reason)
                .refusalResponse(refusalResponse)
                .build();
    }
}
