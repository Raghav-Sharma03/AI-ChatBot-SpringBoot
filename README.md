# AI Chatbot - Spring Boot

A free AI chatbot built with Java, Spring Boot and Spring AI.
Powered by Groq API (Llama 3.1) - completely free, no cost.

## Tech Stack
- Java 21
- Spring Boot 4.1.0
- Spring AI 2.0.0
- Groq API (free tier)

## Setup
1. Clone the repo
2. Copy `src/main/resources/application.properties.example`
   to `src/main/resources/application.properties`
3. Get your free Groq API key from https://console.groq.com
4. Add your key in `application.properties`
5. Run `mvn spring-boot:run`

## API Endpoints
- GET `/api/chat?message=Hello` — send a message and get AI response

## What I learned building this
- Spring Boot REST API setup
- Spring AI integration with external LLM providers
- Connecting to Groq's free API (OpenAI compatible)