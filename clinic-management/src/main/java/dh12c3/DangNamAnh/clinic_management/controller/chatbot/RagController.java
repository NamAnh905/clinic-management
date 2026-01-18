package dh12c3.DangNamAnh.clinic_management.controller.chatbot;

import dh12c3.DangNamAnh.clinic_management.dto.request.chatbot.ChatRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.chatbot.ChatResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.ApiResponse;
import dh12c3.DangNamAnh.clinic_management.service.chatbot.ChatService;
import dh12c3.DangNamAnh.clinic_management.service.chatbot.RagDataIngestor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@CrossOrigin("*")
public class RagController {

    private final RagDataIngestor ragDataIngestor;
    private final ChatService chatService;

    @PostMapping("/ingest")
    public ApiResponse<String> ingestData() {
        ragDataIngestor.ingestAllData();
        return ApiResponse.<String>builder()
                .result("Đồng bộ dữ liệu AI thành công")
                .build();
    }

    @PostMapping("/ask")
    public ApiResponse<ChatResponse> ask(@RequestBody ChatRequest request) {
        String aiAnswer = chatService.chatWithRAG(request.getQuestion());

        return ApiResponse.<ChatResponse>builder()
                .result(ChatResponse.builder()
                        .answer(aiAnswer)
                        .build())
                .build();
    }
}