package dh12c3.DangNamAnh.clinic_management.service.chatbot;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dh12c3.DangNamAnh.clinic_management.dto.response.chatbot.KnowledgeResponse;
import dh12c3.DangNamAnh.clinic_management.entity.chatbot.KnowledgeBase;
import dh12c3.DangNamAnh.clinic_management.repository.chatbot.KnowledgeBaseRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KnowledgeService {

    KnowledgeBaseRepository repository;
    EmbeddingStore<TextSegment> embeddingStore;
    EmbeddingModel embeddingModel;

    @Transactional
    public KnowledgeBase addKnowledge(String question, String answer, String type) { // Thêm tham số type
        // 1. Lưu MySQL
        KnowledgeBase kb = KnowledgeBase.builder()
                .question(question)
                .answer(answer)
                .type(type) // Lưu type động
                .isActive(true)
                .build();
        repository.save(kb);

        // 2. Lưu Qdrant
        String content = "Câu hỏi: " + question + ". Trả lời: " + answer;
        Metadata metadata = Metadata.from("type", type).add("db_id", kb.getId().toString()); // Lưu type vào metadata

        TextSegment segment = TextSegment.from(content, metadata);
        embeddingStore.add(embeddingModel.embed(segment).content(), segment);

        return kb;
    }

    public List<KnowledgeResponse> findAll() {
        return repository.findAll().stream()
                .filter(kb -> kb.isActive())
                .map(kb -> KnowledgeResponse.builder()
                        .id(kb.getId())
                        .question(kb.getQuestion())
                        .answer(kb.getAnswer())
                        .type(kb.getType())
                        .isActive(kb.isActive())
                        .build())
                .collect(Collectors.toList());
    }

    // Hàm xóa kiến thức (Optional - Để làm sau)
    @Transactional
    public void deleteKnowledge(Long id) {
        repository.deleteById(id);
    }
}