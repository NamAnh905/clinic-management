package dh12c3.DangNamAnh.clinic_management.controller.chatbot;

import dh12c3.DangNamAnh.clinic_management.dto.request.chatbot.KnowledgeRequest;
import dh12c3.DangNamAnh.clinic_management.entity.chatbot.KnowledgeBase;
import dh12c3.DangNamAnh.clinic_management.service.chatbot.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/knowledge")
@RequiredArgsConstructor
@CrossOrigin("*")
public class KnowledgeController {
    private final KnowledgeService knowledgeService;

    @PostMapping("/add")
    public ResponseEntity<?> addKnowledge(@RequestBody KnowledgeRequest request) {
        // Cho phép Admin tự điền type (ví dụ: "medicine", "promotion", "facility"...)
        // Nếu không điền thì mặc định là "knowledge"
        String type = (request.getType() != null && !request.getType().isEmpty())
                ? request.getType() : "knowledge";

        KnowledgeBase kb = knowledgeService.addKnowledge(
                request.getQuestion(),
                request.getAnswer(),
                type // <--- Truyền type động vào đây
        );
        return ResponseEntity.ok("✅ Đã nạp kiến thức loại: " + type);
    }
}
