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
public class DocumentDto {

    private String id;
    private String filename;
    private String contentType;
    private Long size;
    private String status;
    private Integer chunkCount;
    private LocalDateTime createdAt;
}
