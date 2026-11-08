package com.cm.spring_ai_demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringAiDemoApplication {

	public static void main(String[] args) {
		sanitizeEnvironment();
		SpringApplication.run(SpringAiDemoApplication.class, args);
	}

	private static void sanitizeEnvironment() {
		String apiKey = getRawProperty("GOOGLE_API_KEY");
		if (apiKey == null || apiKey.isBlank()) {
			apiKey = getRawProperty("GEMINI_API_KEY");
		}
		if (apiKey != null && !apiKey.isBlank()) {
			String cleanKey = apiKey.replaceAll("[\\r\\n\\t\\s]+", "").trim();
			System.setProperty("GOOGLE_API_KEY", cleanKey);
			System.setProperty("GEMINI_API_KEY", cleanKey);
			System.setProperty("spring.ai.google.genai.api-key", cleanKey);
			System.setProperty("spring.ai.google.genai.embedding.api-key", cleanKey);
		}

		String projectId = getRawProperty("GOOGLE_PROJECT_ID");
		if (projectId != null && !projectId.isBlank()) {
			String cleanProject = projectId.replaceAll("[\\r\\n\\t\\s]+", "").trim();
			System.setProperty("GOOGLE_PROJECT_ID", cleanProject);
			System.setProperty("spring.ai.google.genai.project-id", cleanProject);
		}
	}

	private static String getRawProperty(String key) {
		String val = System.getProperty(key);
		if (val == null || val.isBlank()) {
			val = System.getenv(key);
		}
		return val;
	}

}
