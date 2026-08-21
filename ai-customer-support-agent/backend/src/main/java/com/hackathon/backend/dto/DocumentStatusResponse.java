package com.hackathon.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentStatusResponse {

    private String documentId;
    private String filename;
    private String status;
    private String errorMessage;
    private Integer chunkCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
