package com.cm.spring_ai_demo.controller;

import com.cm.spring_ai_demo.dto.DocumentInfo;
import com.cm.spring_ai_demo.dto.RagResponse;
import com.cm.spring_ai_demo.service.RagService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/rag")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    /**
     * Upload and index a PDF document for RAG semantic search.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadPdf(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Please select a valid PDF file to upload."
            ));
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".pdf")) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", "Only PDF files are supported."
            ));
        }

        try {
            DocumentInfo docInfo = ragService.processAndIndexPdf(file);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "PDF successfully processed, chunked, and embedded into VectorStore.",
                    "document", docInfo
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", e.getMessage()
            ));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Failed to parse PDF document: " + e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "status", "error",
                    "message", "Failed to process/embed PDF: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName())
            ));
        }
    }

    /**
     * Ask a question grounded in the uploaded PDF documents.
     */
    @GetMapping("/chat")
    public ResponseEntity<RagResponse> askRag(
            @RequestParam("message") String message,
            @RequestParam(value = "userId", defaultValue = "OmniUser") String userId
    ) {
        RagResponse response = ragService.askWithGrounding(message, userId);
        return ResponseEntity.ok(response);
    }

    /**
     * List all currently uploaded and indexed documents.
     */
    @GetMapping("/documents")
    public ResponseEntity<List<DocumentInfo>> getDocuments() {
        return ResponseEntity.ok(ragService.getIndexedDocuments());
    }

    /**
     * Clear all indexed documents.
     */
    @DeleteMapping("/clear")
    public ResponseEntity<Map<String, String>> clearIndex() {
        ragService.clearIndex();
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "VectorStore document index cleared successfully."
        ));
    }
}
