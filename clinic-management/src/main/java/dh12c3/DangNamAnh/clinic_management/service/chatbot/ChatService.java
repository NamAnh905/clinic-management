package dh12c3.DangNamAnh.clinic_management.service.chatbot;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage; // Thêm import này
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dh12c3.DangNamAnh.clinic_management.helper.ChatPromptUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList; // Thêm import này
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatService {

    EmbeddingStore<TextSegment> embeddingStore;
    EmbeddingModel embeddingModel;
    ChatLanguageModel chatLanguageModel;

    Map<String, ChatMemory> chatMemories = new ConcurrentHashMap<>();

    // --- HÀM 1: VIẾT LẠI CÂU HỎI BẰNG PROMPT TÁCH RIÊNG ---
    private String rewriteQuery(ChatMemory chatMemory, String userMessage) {
        if (chatMemory.messages().isEmpty()) {
            return userMessage;
        }

        StringBuilder history = new StringBuilder();
        for (ChatMessage msg : chatMemory.messages()) {
            if (msg instanceof UserMessage) {
                history.append("Bệnh nhân: ").append(msg.text()).append("\n");
            } else if (msg instanceof AiMessage) {
                history.append("AI: ").append(msg.text()).append("\n");
            }
        }

        // Gọi Prompt từ file Utils
        String rewritePrompt = String.format(ChatPromptUtils.REWRITE_QUERY_PROMPT, history.toString(), userMessage);

        try {
            String rewritten = chatLanguageModel.generate(rewritePrompt);
            log.info("🔄 Query Rewriting: [{}] ---> [{}]", userMessage, rewritten);
            return rewritten;
        } catch (Exception e) {
            log.warn("⚠️ Lỗi khi viết lại câu hỏi, dùng câu gốc: ", e);
            return userMessage;
        }
    }

    // --- HÀM 2: LUỒNG CHAT CHÍNH ---
    public String chatWithRAG(String sessionId, String userMessage) {
        log.info("Session: {} | User Question: {}", sessionId, userMessage);

        // Giảm số lượng tin nhắn nhớ xuống 6 để tối ưu tốc độ và chi phí
        ChatMemory chatMemory = chatMemories.computeIfAbsent(sessionId,
                id -> MessageWindowChatMemory.withMaxMessages(6));

        // 1. Phân tích ngữ cảnh và viết lại câu hỏi
        String standaloneQuery = rewriteQuery(chatMemory, userMessage);

        // 2. Tìm kiếm Vector bằng câu hỏi chuẩn
        List<EmbeddingMatch<TextSegment>> allMatches = embeddingStore.findRelevant(
                embeddingModel.embed(standaloneQuery).content(),
                60,
                0.55
        );

        // 3. Sàng lọc thông minh
        StringBuilder contextBuilder = new StringBuilder();
        Map<String, Integer> typeCounters = new HashMap<>();

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

            if (currentCount < maxLimit || (isSuperRelevant && currentCount < maxLimit + 2)) {
                contextBuilder.append(text).append("\n---\n");
                typeCounters.put(type, currentCount + 1);
            }
        }

        String context = contextBuilder.toString();

        if (context.isEmpty()) {
            log.info("RAG Miss -> Từ chối trả lời câu hỏi ngoài luồng");
            return "Xin lỗi bạn, tôi là trợ lý ảo của phòng khám 28Care. Tôi chỉ có thể hỗ trợ giải đáp các thông tin liên quan đến dịch vụ y tế, chuyên khoa, bác sĩ, và quy trình khám chữa bệnh tại đây.";
        }

        // 4. Gọi AI
        String finalPrompt = String.format(ChatPromptUtils.PROMPT_TEMPLATE, context, userMessage);

        // TẠO BẢN SAO BỘ NHỚ TẠM THỜI ĐỂ TRÁNH LỖI TOKEN BLOAT
        List<ChatMessage> tempMessages = new ArrayList<>(chatMemory.messages());
        tempMessages.add(UserMessage.from(finalPrompt));

        try {
            Response<AiMessage> aiResponse = chatLanguageModel.generate(tempMessages);
            String answerText = aiResponse.content().text();

            // QUAN TRỌNG: Chỉ lưu CÂU HỎI NGẮN và TRẢ LỜI NGẮN vào bộ nhớ chính thức
            chatMemory.add(UserMessage.from(userMessage));
            chatMemory.add(aiResponse.content());

            return answerText;

        } catch (Exception e) {
            log.error("Lỗi khi gọi AI: ", e);
            return "Hệ thống đang bận, vui lòng thử lại sau.";
        }
    }
}