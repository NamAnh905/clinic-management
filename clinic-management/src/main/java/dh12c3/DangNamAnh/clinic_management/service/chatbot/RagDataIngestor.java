package dh12c3.DangNamAnh.clinic_management.service.chatbot;

import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.chatbot.KnowledgeResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.master.DrugResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.staff.DoctorResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.master.SpecialtyResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.master.ServiceEntityResponse;
// Import các Service hiện có
import dh12c3.DangNamAnh.clinic_management.service.master.DrugService;
import dh12c3.DangNamAnh.clinic_management.service.staff.DoctorService;
import dh12c3.DangNamAnh.clinic_management.service.master.SpecialtyService;
import dh12c3.DangNamAnh.clinic_management.service.master.SEService;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingStore;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RagDataIngestor {

    EmbeddingStore<TextSegment> embeddingStore;
    EmbeddingModel embeddingModel;

    DoctorService doctorService;
    SpecialtyService specialtyService;
    SEService seService;
    DrugService drugService;
    KnowledgeService knowledgeService;

    public void ingestAllData() {
        log.info("--- BẮT ĐẦU ĐỒNG BỘ DỮ LIỆU SANG AI (QDRANT) ---");

        // 1. Nạp Bác sĩ
        // SỬA: .getItems() -> .getData()
        PageResponse<DoctorResponse> doctorPage = doctorService.findAllDoctors(null, "", 1, 1000);
        List<DoctorResponse> doctors = doctorPage.getData();

        for (DoctorResponse doc : doctors) {
            String text = String.format("Bác sĩ: %s. Chuyên khoa: %s. Giới tính: %s.",
                    doc.getFullName(),
                    doc.getSpecialtyName(),
                    doc.getGender()
            );

            Metadata metadata = Metadata.from("type", "doctor").add("id", doc.getDoctorId());
            embedAndStore(text, metadata);
        }
        log.info("Đã nạp {} bác sĩ.", doctors.size());

        // 2. Nạp Chuyên khoa
        // SỬA: .getItems() -> .getData()
        PageResponse<SpecialtyResponse> specPage = specialtyService.findAll("", 1, 1000);
        for (SpecialtyResponse spec : specPage.getData()) {
            String text = String.format("Chuyên khoa: %s. Thông tin chi tiết: %s",
                    spec.getName(),
                    spec.getDescription()
            );

            Metadata metadata = Metadata.from("type", "specialty").add("id", spec.getSpecialtyId());
            embedAndStore(text, metadata);
        }

        // 3. Nạp Dịch vụ (ServiceEntity)
        // SỬA: .getItems() -> .getData()
        PageResponse<ServiceEntityResponse> servicePage = seService.findAll(null, "", 1, 1000);
        for (ServiceEntityResponse ser : servicePage.getData()) {
            String text = String.format("Dịch vụ: %s. Loại dịch vụ: %s. Giá tiền: %s VNĐ.",
                    ser.getName(),
                    ser.getType(),
                    ser.getPrice()
            );

            Metadata metadata = Metadata.from("type", "service").add("id", ser.getServiceId());
            embedAndStore(text, metadata);
        }

        PageResponse<DrugResponse> drugPage = drugService.findAll("", 1, 1000);
        List<DrugResponse> drugs = drugPage.getData(); // Lưu ý: check xem PageResponse dùng .getData() hay .getContent()

        for (DrugResponse drug : drugs) {
            String text = String.format("Thuốc: %s. Đơn vị tính: %s. Giá tham khảo: %s VNĐ. Hướng dẫn/Công dụng: %s",
                    drug.getName(),
                    drug.getUnit(),
                    drug.getPrice(),
                    drug.getInstructions() != null ? drug.getInstructions() : "Theo chỉ định của bác sĩ"
            );

            Metadata metadata = Metadata.from("type", "drug").add("id", drug.getDrugId());
            embedAndStore(text, metadata);
        }
        log.info("Đã nạp {} loại thuốc.", drugs.size());

        // 5. NẠP KIẾN THỨC CHUNG (Từ bảng 'knowledge_base')
        // Dựa trên file SQL: cột question, answer
        List<KnowledgeResponse> kbs = knowledgeService.findAll(); // Bạn tự viết hàm lấy hết
        for (KnowledgeResponse kb : kbs) {
            String text = String.format("Câu hỏi thường gặp: %s\nTrả lời: %s",
                    kb.getQuestion(),
                    kb.getAnswer()
            );
            Metadata metadata = Metadata.from("type", "faq").add("id", kb.getId());
            embedAndStore(text, metadata);
        }
        log.info("Đã nạp {} câu hỏi FAQ.", kbs.size());

        log.info("--- HOÀN TẤT ĐỒNG BỘ DỮ LIỆU ---");
    }

    private void embedAndStore(String text, Metadata metadata) {
        TextSegment segment = TextSegment.from(text, metadata);
        Embedding embedding = embeddingModel.embed(segment).content();
        embeddingStore.add(embedding, segment);
    }
}