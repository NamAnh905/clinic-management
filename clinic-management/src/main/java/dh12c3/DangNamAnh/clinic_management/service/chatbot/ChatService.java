package dh12c3.DangNamAnh.clinic_management.service.chatbot;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dh12c3.DangNamAnh.clinic_management.helper.ChatPromptUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final EmbeddingStore<TextSegment> embeddingStore;
    private final EmbeddingModel embeddingModel;
    private final ChatLanguageModel chatLanguageModel;

    public String chatWithRAG(String userMessage) {
        log.info("User Question: {}", userMessage);

        // 1. TẠO LƯỚI "TINH GỌN" (Fetch 25, MinScore 0.65)
        // Thay đổi: Giảm số lượng, tăng độ chính xác.
        List<EmbeddingMatch<TextSegment>> allMatches = embeddingStore.findRelevant(
                embeddingModel.embed(userMessage).content(),
                25,   // Giảm từ 70 -> 25
                0.65  // Tăng từ 0.4 -> 0.65 (Chỉ lấy tin tin cậy)
        );

        // 2. SÀNG LỌC THÔNG MINH (Smart Filtering)
        StringBuilder contextBuilder = new StringBuilder();

        // Map để kiểm soát số lượng, nhưng không ép buộc quota
        Map<String, Integer> typeCounters = new HashMap<>();

        System.out.println("--- KẾT QUẢ SÀNG LỌC MỚI ---");

        for (EmbeddingMatch<TextSegment> match : allMatches) {
            String text = match.embedded().text();
            String type = match.embedded().metadata().getString("type");
            if (type == null) type = "other";

            // Cấu hình giới hạn trần (Max Limit) - Không phải mục tiêu bắt buộc
            int maxLimit = switch (type) {
                case "drug" -> 5;      // Chỉ lấy tối đa 5 thuốc
                case "service" -> 3;   // Chỉ lấy tối đa 3 dịch vụ sát nhất
                case "doctor" -> 2;    // Chỉ lấy 2 bác sĩ sát nhất
                case "specialty" -> 2; // Chỉ lấy 2 chuyên khoa
                default -> 2;
            };

            int currentCount = typeCounters.getOrDefault(type, 0);

            // LOGIC QUYẾT ĐỊNH:
            // 1. Nếu chưa chạm trần -> Lấy.
            // 2. Nếu điểm cực cao (>0.85) -> Vẫn lấy thêm dù đã chạm trần (Ưu tiên tuyệt đối).
            boolean isSuperRelevant = match.score() > 0.85;

            if (currentCount < maxLimit || isSuperRelevant) {
                contextBuilder.append(text).append("\n---\n");
                typeCounters.put(type, currentCount + 1);

                // Log kiểm tra
                System.out.printf("[%s] Score: %.4f | %s...%n", type.toUpperCase(), match.score(), text.substring(0, Math.min(text.length(), 50)));
            }
        }

        String context = contextBuilder.toString();

        // Fallback: Nếu lọc kỹ quá mà không còn gì -> Hạ chuẩn để tìm lại (Optional)
        if (context.isEmpty()) {
            return "Hiện tại tôi không tìm thấy thông tin chính xác về vấn đề này trong hệ thống.";
        }

        // 3. GỌI AI (Giữ nguyên)
        String finalPrompt = String.format(ChatPromptUtils.PROMPT_TEMPLATE,
                ChatPromptUtils.getClinicInfo(),
                context,
                userMessage
        );

        try {
            return chatLanguageModel.generate(finalPrompt);
        } catch (Exception e) {
            log.error("Lỗi khi gọi AI: ", e);
            return "Hệ thống đang bận, vui lòng thử lại sau.";
        }
    }
}