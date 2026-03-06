package dh12c3.DangNamAnh.clinic_management.service.chatbot;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dh12c3.DangNamAnh.clinic_management.dto.response.master.DrugResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.master.ServiceEntityResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.master.SpecialtyResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.staff.DoctorResponse;
import dh12c3.DangNamAnh.clinic_management.entity.chatbot.VectorMapping;
import dh12c3.DangNamAnh.clinic_management.event.VectorSyncEvent;
import dh12c3.DangNamAnh.clinic_management.repository.chatbot.VectorMappingRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Collections;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VectorSyncService {

    EmbeddingStore<TextSegment> embeddingStore;
    EmbeddingModel embeddingModel;

    // TIÊM REPOSITORY VÀO ĐÂY
    VectorMappingRepository vectorMappingRepository;

    // Cho RagDataIngestor gọi để lưu xuống DB
    @Transactional
    public void saveVectorId(String type, Long id, String qdrantId) {
        vectorMappingRepository.save(VectorMapping.builder()
                .entityType(type)
                .entityId(id)
                .qdrantId(qdrantId)
                .build());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleVectorSyncEvent(VectorSyncEvent event) {
        try {
            if ("DELETE".equals(event.getAction())) {
                deleteVector(event.getType(), event.getId());
                return;
            }

            if ("UPDATE".equals(event.getAction())) {
                switch (event.getType()) {
                    case "drug" -> syncDrug((DrugResponse) event.getPayload());
                    case "doctor" -> syncDoctor((DoctorResponse) event.getPayload());
                    case "service" -> syncService((ServiceEntityResponse) event.getPayload());
                    case "specialty" -> syncSpecialty((SpecialtyResponse) event.getPayload());
                    default -> log.warn("Loại chưa hỗ trợ đồng bộ: {}", event.getType());
                }
            }
        } catch (Exception e) {
            log.error("Lỗi ngầm đồng bộ AI: ", e);
        }
    }

    private void deleteVector(String type, Long id) {
        try {
            // Lấy ID Qdrant từ CSDL MySQL ra
            Optional<VectorMapping> mappingOpt = vectorMappingRepository.findByEntityTypeAndEntityId(type, id);

            if (mappingOpt.isPresent()) {
                String qdrantId = mappingOpt.get().getQdrantId();
                // 1. Xóa trong Qdrant
                embeddingStore.removeAll(Collections.singletonList(qdrantId));
                // 2. Xóa vết trong MySQL
                vectorMappingRepository.deleteByEntityTypeAndEntityId(type, id);
                log.info("🗑️ Đã xóa Vector [{}] ID: {}", type, id);
            } else {
                log.warn("⚠️ Không tìm thấy mapping trong DB để xóa. (Có thể là dữ liệu mới)");
            }
        } catch (Exception e) {
            log.warn("⚠️ Lỗi khi xóa Vector: {}", e.getMessage());
        }
    }

    private void syncDrug(DrugResponse drug) {
        deleteVector("drug", drug.getDrugId());
        String text = String.format("Thuốc: %s. Đơn vị tính: %s. Giá tham khảo: %s VNĐ. Hướng dẫn sử dụng: %s",
                drug.getName(), drug.getUnit() != null ? drug.getUnit() : "Viên/Chai", drug.getPrice(), drug.getInstructions() != null ? drug.getInstructions() : "Theo chỉ định bác sĩ");
        embedAndSave(text, "drug", drug.getDrugId(), drug.getName());
    }

    private void syncDoctor(DoctorResponse doc) {
        deleteVector("doctor", doc.getDoctorId());
        String text = String.format("Bác sĩ %s. Chuyên khoa %s. Giới tính: %s.",
                doc.getFullName(), doc.getSpecialtyName(), doc.getGender() != null ? doc.getGender().name() : "Chưa cập nhật");
        embedAndSave(text, "doctor", doc.getDoctorId(), doc.getFullName());
    }

    private void syncService(ServiceEntityResponse service) {
        deleteVector("service", service.getServiceId());
        String text = String.format("Gói dịch vụ y tế: %s. Phân loại: %s. Chi phí/Giá tiền: %s VNĐ.",
                service.getName(), service.getType() != null ? service.getType().name() : "Khám bệnh", service.getPrice());
        embedAndSave(text, "service", service.getServiceId(), service.getName());
    }

    private void syncSpecialty(SpecialtyResponse spec) {
        deleteVector("specialty", spec.getSpecialtyId());
        String text = String.format("Chuyên khoa: %s. Thông tin chi tiết: %s.",
                spec.getName(), spec.getDescription() != null ? spec.getDescription() : "Khám và điều trị các bệnh liên quan.");
        embedAndSave(text, "specialty", spec.getSpecialtyId(), spec.getName());
    }

    private void embedAndSave(String text, String type, Long id, String entityName) {
        Metadata metadata = Metadata.from("type", type).add("id", String.valueOf(id));
        TextSegment segment = TextSegment.from(text, metadata);
        dev.langchain4j.data.embedding.Embedding embedding = embeddingModel.embed(segment).content();

        // 1. Thêm vào Qdrant
        String qdrantId = embeddingStore.add(embedding, segment);

        // 2. Lưu VĨNH VIỄN xuống MySQL thay vì dùng RAM
        saveVectorId(type, id, qdrantId);

        log.info("✅ Qdrant Sync Thành công: {} [{}]", type.toUpperCase(), entityName);
    }
}