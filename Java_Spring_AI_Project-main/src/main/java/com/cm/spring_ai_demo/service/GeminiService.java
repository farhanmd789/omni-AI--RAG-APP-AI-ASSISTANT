package com.cm.spring_ai_demo.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;


// Updated for 1.1.2 compatibility
import static org.springframework.ai.chat.memory.ChatMemory.CONVERSATION_ID;


@Service // Hiring the staff!
public class GeminiService {
    
    private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    
    
    public GeminiService(ChatClient.Builder builder, ChatMemory chatMemory) {
        this.chatMemory = chatMemory;
        this.chatClient = builder
                .defaultSystem("You are OmniAI, an intelligent Senior Developer and AI assistant.")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
        System.out.println("API KEY IS: " + System.getenv("GEMINI_API_KEY"));
    }

    public String generateResponse(String userId, String message) {
        logger.info("Thread: {} | Processing Chat for User: {}", Thread.currentThread().getName(), userId);

        return chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(CONVERSATION_ID, userId))
                .call()
                .content();
    }

    public void clearHistory(String userId) {
        chatMemory.clear(userId);
        logger.info("Memory cleared for User: {}", userId);
    }
    



}





// package com.cm.spring_ai_demo.service;

// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.ai.chat.client.ChatClient;
// import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
// import org.springframework.ai.chat.memory.ChatMemory;
// import org.springframework.stereotype.Service;

// import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_ID_SIZE;

// @Service
// public class GeminiService {

//     private static final Logger logger = LoggerFactory.getLogger(GeminiService.class);
//     private final ChatClient chatClient;
//     private final ChatMemory chatMemory;

//     public GeminiService(ChatClient.Builder builder, ChatMemory chatMemory) {
//         this.chatMemory = chatMemory;
//         this.chatClient = builder
//                 // 1. Give the AI a permanent "System" identity
//                 .defaultSystem("You are OmniAI, an intelligent Senior Developer and AI assistant.")
//                 // 2. Attach the Memory Advisor (Keep last 10 messages for token efficiency)
//                 .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
//                 .build();
//     }

//     public String generateResponse(String userId, String message) {
//         logger.info("Thread: {} | Processing Chat for User: {}", Thread.currentThread().getName(), userId);

//         return chatClient.prompt()
//                 .user(message)
//                 // 3. Bind the request to a specific conversation ID
//                 .advisors(a -> a.param(CHAT_ID_SIZE, userId))
//                 .call()
//                 .content();
//     }

//     public void clearHistory(String userId) {
//         chatMemory.clear(userId);
//         logger.info("Memory cleared for User: {}", userId);
//     }
// }
