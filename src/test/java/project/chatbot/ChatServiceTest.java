package project.chatbot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import project.chatbot.service.ChatService;
import project.chatbot.service.RagService;
import org.springframework.ai.document.Document;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ChatServiceTest {

    @Mock
    private ChatClient geminiClient;

    @Mock
    private ChatClient openRouterClient;

    @Mock
    private ChatClient ollamaClient;

    @Mock
    private ChatMemory chatMemory;

    @Mock
    private RagService ragService;

    @InjectMocks
    private ChatService chatService;

    @Test
    void whenRagReturnsDocuments_thenContextIsAdded() {
        Document doc = new Document("Spring AI is a framework.");
        when(ragService.search("what is spring ai", "session-1")).thenReturn(List.of(doc));
        when(geminiClient.prompt()).thenThrow(new RuntimeException("Gemini down"));
        when(openRouterClient.prompt()).thenThrow(new RuntimeException("OpenRouter down"));
        when(ollamaClient.prompt()).thenThrow(new RuntimeException("Ollama down"));

        assertThrows(RuntimeException.class,
                () -> chatService.chat("what is spring ai", "session-1"));

        verify(ragService, times(1)).search("what is spring ai", "session-1");
    }

    @Test
    void whenRagReturnsNoDocuments_thenNoContextAdded() {
        when(ragService.search("hello", "session-1")).thenReturn(List.of());
        when(geminiClient.prompt()).thenThrow(new RuntimeException("Gemini down"));
        when(openRouterClient.prompt()).thenThrow(new RuntimeException("OpenRouter down"));
        when(ollamaClient.prompt()).thenThrow(new RuntimeException("Ollama down"));

        assertThrows(RuntimeException.class,
                () -> chatService.chat("hello", "session-1"));

        verify(ragService, times(1)).search("hello", "session-1");
    }

    @Test
    void whenGeminiFails_thenOpenRouterIsCalled() {
        when(ragService.search(anyString(), anyString())).thenReturn(List.of());
        when(geminiClient.prompt()).thenThrow(new RuntimeException("Gemini down"));
        when(openRouterClient.prompt()).thenThrow(new RuntimeException("OpenRouter down"));
        when(ollamaClient.prompt()).thenThrow(new RuntimeException("Ollama down"));

        assertThrows(RuntimeException.class,
                () -> chatService.chat("hello", "session-1"));

        verify(openRouterClient, atLeastOnce()).prompt();
    }
}