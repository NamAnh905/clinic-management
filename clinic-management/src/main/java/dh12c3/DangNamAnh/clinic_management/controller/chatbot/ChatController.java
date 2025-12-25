package dh12c3.DangNamAnh.clinic_management.controller.chatbot;

import dh12c3.DangNamAnh.clinic_management.dto.request.chatbot.ChatRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.chatbot.ChatResponse;
import dh12c3.DangNamAnh.clinic_management.service.chatbot.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/ask")
    public ChatResponse askAI(@RequestBody ChatRequest request) {
        String answer = chatService.getAnswer(request.getMessage());
        return new ChatResponse(answer);
    }
}