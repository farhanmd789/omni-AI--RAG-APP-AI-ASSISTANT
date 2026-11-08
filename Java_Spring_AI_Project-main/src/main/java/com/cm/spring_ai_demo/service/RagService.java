package com.cm.spring_ai_demo.service;

import com.cm.spring_ai_demo.dto.DocumentInfo;
import com.cm.spring_ai_demo.dto.RagResponse;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;

@Service
public class RagService {

    private static final Logger logger = LoggerFactory.getLogger(RagService.class);

    private final VectorStore vectorStore;
    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final TokenTextSplitter tokenTextSplitter;
    private final List<DocumentInfo> indexedDocuments = new CopyOnWriteArrayList<>();

    public RagService(
            VectorStore vectorStore,
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory,
            TokenTextSplitter tokenTextSplitter
    ) {
        this.vectorStore = vectorStore;
        this.chatMemory = chatMemory;
        this.tokenTextSplitter = tokenTextSplitter;
        this.chatClient = chatClientBuilder
                .defaultSystem("You are OmniAI, an expert AI assistant that provides strictly source-grounded answers based on user-provided documents.")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    /**
     * Parse an uploaded PDF file, extract text per page, chunk the content,
     * generate embeddings, and store them in the VectorStore.
     */
    public DocumentInfo processAndIndexPdf(MultipartFile file) throws IOException {
        String rawFilename = file.getOriginalFilename();
        final String docName = (rawFilename != null && !rawFilename.isBlank()) ? rawFilename : "uploaded_document.pdf";

        logger.info("Processing and indexing PDF: {}", docName);

        List<Document> pageDocuments = new ArrayList<>();
        int pageCount = 0;

        try (InputStream inputStream = file.getInputStream();
             PDDocument pdDocument = PDDocument.load(inputStream)) {

            pageCount = pdDocument.getNumberOfPages();
            PDFTextStripper stripper = new PDFTextStripper();

            for (int page = 1; page <= pageCount; page++) {
                stripper.setStartPage(page);
                stripper.setEndPage(page);
                String text = stripper.getText(pdDocument);

                if (text != null && !text.isBlank()) {
                    Map<String, Object> metadata = new HashMap<>();
                    metadata.put("source", docName);
                    metadata.put("page", page);
                    metadata.put("uploadedAt", System.currentTimeMillis());

                    pageDocuments.add(new Document(text.trim(), metadata));
                }
            }
        }

        if (pageDocuments.isEmpty()) {
            throw new IllegalArgumentException("No readable text found in the uploaded PDF file: " + docName);
        }

        // Chunk documents using token-aware text splitting
        List<Document> chunks = tokenTextSplitter.apply(pageDocuments);
        logger.info("Generated {} chunks from {} pages in {}", chunks.size(), pageCount, docName);

        // Add chunks to the vector store (generates embeddings automatically)
        vectorStore.add(chunks);

        DocumentInfo docInfo = new DocumentInfo(docName, pageCount, chunks.size(), System.currentTimeMillis());
        // Remove prior entry if same file was re-uploaded
        indexedDocuments.removeIf(d -> d.filename().equalsIgnoreCase(docName));
        indexedDocuments.add(docInfo);

        logger.info("Successfully indexed PDF {} into VectorStore", docName);
        return docInfo;
    }

    /**
     * Query the vector store for top semantic matches, construct a grounded prompt,
     * and generate an answer citing the retrieved sources.
     */
    public RagResponse askWithGrounding(String query, String userId) {
        logger.info("RAG Query from user '{}': {}", userId, query);

        if (indexedDocuments.isEmpty()) {
            return new RagResponse(
                    "no_documents",
                    "No PDF documents have been uploaded yet. Please upload a PDF to ask questions grounded in your documents.",
                    Collections.emptyList(),
                    query
            );
        }

        // Semantic Search in Vector Store
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(4)
                .build();

        List<Document> relevantDocs = vectorStore.similaritySearch(searchRequest);

        if (relevantDocs.isEmpty()) {
            return new RagResponse(
                    "not_found",
                    "I searched the uploaded documents, but could not find relevant content matching your question.",
                    Collections.emptyList(),
                    query
            );
        }

        // Build grounded context
        StringBuilder contextBuilder = new StringBuilder();
        List<Map<String, Object>> sourceCitations = new ArrayList<>();

        for (Document doc : relevantDocs) {
            String source = (String) doc.getMetadata().getOrDefault("source", "PDF");
            Object page = doc.getMetadata().getOrDefault("page", 1);

            contextBuilder.append(String.format("--- [Source: %s | Page: %s] ---\n%s\n\n", source, page, doc.getText()));

            Map<String, Object> citation = new HashMap<>();
            citation.put("source", source);
            citation.put("page", page);
            String snippet = doc.getText();
            if (snippet.length() > 220) {
                snippet = snippet.substring(0, 220) + "...";
            }
            citation.put("snippet", snippet);
            sourceCitations.add(citation);
        }

        String promptInstructions = """
                You are OmniAI, an intelligent research assistant.
                Answer the user's question accurately using ONLY the provided document context below.
                
                Rules:
                1. Strictly ground your answer in the provided document chunks.
                2. If the context does not contain the answer, say: "The provided document(s) do not contain information to answer this question."
                3. Do not invent facts or extrapolate beyond what is stated in the context.
                4. At the end of your explanation, list the sources and page numbers you referenced.
                
                Retrieved Document Context:
                %s
                
                User Question: %s
                """.formatted(contextBuilder.toString(), query);

        String conversationId = (userId != null && !userId.isBlank()) ? userId + "-rag" : "default-rag";

        String answer = chatClient.prompt()
                .user(promptInstructions)
                .advisors(a -> a.param(CONVERSATION_ID, conversationId))
                .call()
                .content();

        return new RagResponse("success", answer, sourceCitations, query);
    }

    /**
     * Retrieve list of all currently indexed documents.
     */
    public List<DocumentInfo> getIndexedDocuments() {
        return Collections.unmodifiableList(indexedDocuments);
    }

    /**
     * Clear all indexed documents from tracking.
     */
    public void clearIndex() {
        indexedDocuments.clear();
        logger.info("Cleared all indexed document metadata.");
    }
}
