package com.cm.spring_ai_demo.dto;

public record DocumentInfo(
        String filename,
        int pageCount,
        int chunkCount,
        long uploadedAt
) {}
