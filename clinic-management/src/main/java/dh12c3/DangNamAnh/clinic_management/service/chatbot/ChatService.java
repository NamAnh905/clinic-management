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

        List<EmbeddingMatch<TextSegment>> allMatches = embeddingStore.findRelevant(
                embeddingModel.embed(userMessage).content(),
                60,
                0.55
        );

        // 2. SÀNG LỌC THÔNG MINH (Smart Filtering)
        StringBuilder contextBuilder = new StringBuilder();
        Map<String, Integer> typeCounters = new HashMap<>();

        System.out.println("--- KẾT QUẢ SÀNG LỌC MỚI ---");

        for (EmbeddingMatch<TextSegment> match : allMatches) {
            String text = match.embedded().text();
            String type = match.embedded().metadata().getString("type");
            if (type == null) type = "other";

            int maxLimit = switch (type) {
                case "drug" -> 5;
                case "service" -> 3;
                case "doctor" -> 5;
                case "specialty" -> 3;
                default -> 2;
            };

            int currentCount = typeCounters.getOrDefault(type, 0);

            boolean isSuperRelevant = match.score() > 0.92;
            int hardCap = maxLimit + 2;

            if (currentCount < maxLimit || (isSuperRelevant && currentCount < hardCap)) {
                contextBuilder.append(text).append("\n---\n");
                typeCounters.put(type, currentCount + 1);
                System.out.printf("[%s] Score: %.4f | %s...%n", type.toUpperCase(), match.score(), text.substring(0, Math.min(text.length(), 50)));
            }
        }

        String context = contextBuilder.toString();

        if (context.isEmpty()) {
            log.info("RAG Miss -> Chuyển sang chế độ Chat thường (General Knowledge)");

            // Sử dụng Template mới vừa thêm ở ChatPromptUtils
            String generalPrompt = String.format(ChatPromptUtils.GENERAL_PROMPT_TEMPLATE, userMessage);

            try {
                return chatLanguageModel.generate(generalPrompt);
            } catch (Exception e) {
                log.error("Lỗi khi gọi AI (General): ", e);
                return "Hệ thống đang bận, vui lòng thử lại sau.";
            }
        }

        // 3. GỌI AI (Giữ nguyên)
        String finalPrompt = String.format(ChatPromptUtils.PROMPT_TEMPLATE,
                context,       // Tham số 1: Dữ liệu tìm thấy
                userMessage    // Tham số 2: Câu hỏi
        );

        try {
            return chatLanguageModel.generate(finalPrompt);
        } catch (Exception e) {
            log.error("Lỗi khi gọi AI: ", e);
            return "Hệ thống đang bận, vui lòng thử lại sau.";
        }
    }
}