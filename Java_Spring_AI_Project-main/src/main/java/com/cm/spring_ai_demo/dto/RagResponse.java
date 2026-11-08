package com.cm.spring_ai_demo.dto;

import java.util.List;
import java.util.Map;

public record RagResponse(
        String status,
        String answer,
        List<Map<String, Object>> sources,
        String query
) {}
