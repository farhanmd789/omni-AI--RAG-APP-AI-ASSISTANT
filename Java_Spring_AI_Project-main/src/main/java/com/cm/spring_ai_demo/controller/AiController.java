package com.cm.spring_ai_demo.controller;

import com.cm.spring_ai_demo.dto.AiResponse;
import com.cm.spring_ai_demo.service.GeminiService;
import org.springframework.web.bind.annotation.*;

@RestController // The Voice of our app
@RequestMapping("/api/ai")
public class AiController {

    private final GeminiService geminiService;
    public AiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @GetMapping("/chat")
    public AiResponse chat(
            @RequestParam(value = "message") String message,
            @RequestParam(value = "userId", defaultValue = "default-user") String userId) {
        String answer= geminiService.generateResponse(userId, message);
        
        // Spring automatically converts this Record to clean JSON!
        return new AiResponse("success", answer);

    }

    @DeleteMapping("/memory/{userId}")
    public String clearMemory(@PathVariable String userId) {
        geminiService.clearHistory(userId);
        return "Memory cleared successfully for " + userId;
    }
    
}





// package com.cm.spring_ai_demo.controller;

// import com.cm.spring_ai_demo.service.GeminiService;
// import org.springframework.web.bind.annotation.*;

// @RestController
// @RequestMapping("/api/ai")
// public class AiController {

//     private final GeminiService geminiService;

//     public AiController(GeminiService geminiService) {
//         this.geminiService = geminiService;
//     }

//     @GetMapping("/chat")
//     public String chat(
//             @RequestParam(value = "message") String message,
//             @RequestParam(value = "userId", defaultValue = "default-user") String userId) {
//         return geminiService.generateResponse(userId, message);
//     }

//     @DeleteMapping("/memory/{userId}")
//     public String clearMemory(@PathVariable String userId) {
//         geminiService.clearHistory(userId);
//         return "Memory cleared successfully for " + userId;
//     }
// }
