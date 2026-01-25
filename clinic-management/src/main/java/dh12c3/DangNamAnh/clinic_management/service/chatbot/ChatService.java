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
                60,   // Tăng lên để bác sĩ không bị Dịch vụ đẩy ra khỏi top
                0.55  // Hạ xuống để bắt được các kết quả khớp lỏng hơn
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
                case "drug" -> 5;
                case "service" -> 3; // Giữ mức thấp để tránh spam dịch vụ
                case "doctor" -> 5;  // Tăng bác sĩ lên
                case "specialty" -> 3;
                default -> 2;
            };

            int currentCount = typeCounters.getOrDefault(type, 0);

            // LOGIC QUYẾT ĐỊNH:
            // 1. Nếu chưa chạm trần -> Lấy.
            // 2. Nếu điểm cực cao (>0.85) -> Vẫn lấy thêm dù đã chạm trần (Ưu tiên tuyệt đối).
            boolean isSuperRelevant = match.score() > 0.92;
            int hardCap = maxLimit + 2; // Cho phép vượt quota tối đa 2 đơn vị

            if (currentCount < maxLimit || (isSuperRelevant && currentCount < hardCap)) {
                contextBuilder.append(text).append("\n---\n");
                typeCounters.put(type, currentCount + 1);
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