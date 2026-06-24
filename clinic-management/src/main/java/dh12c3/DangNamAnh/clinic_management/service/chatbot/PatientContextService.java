package dh12c3.DangNamAnh.clinic_management.service.chatbot;

import dh12c3.DangNamAnh.clinic_management.entity.medical.MedicalRecord;
import dh12c3.DangNamAnh.clinic_management.entity.medical.Prescription;
import dh12c3.DangNamAnh.clinic_management.entity.medical.PrescriptionDetail;
import dh12c3.DangNamAnh.clinic_management.entity.patient.Patient;
import dh12c3.DangNamAnh.clinic_management.repository.medical.MedicalRecordRepository;
import dh12c3.DangNamAnh.clinic_management.repository.patient.PatientRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class PatientContextService {

    PatientRepository patientRepository;
    MedicalRecordRepository medicalRecordRepository;

    /**
     * Tạo chuỗi context tóm tắt lịch sử khám gần nhất (tối đa 5 lần).
     * Trả về null nếu không tìm thấy patient hoặc không có hồ sơ.
     */
    public String buildPatientContext(Long patientId) {
        if (patientId == null) return null;

        Patient patient = patientRepository.findById(patientId).orElse(null);
        if (patient == null) {
            log.warn("Không tìm thấy bệnh nhân với ID: {}", patientId);
            return null;
        }

        String patientName = patient.getUser().getFullName();

        // Lấy 5 lần khám gần nhất
        Page<MedicalRecord> recentRecords = medicalRecordRepository
                .findByAppointment_Patient_PatientId(
                        patientId,
                        PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "appointment.appointmentTime"))
                );

        if (recentRecords.isEmpty()) {
            return String.format("Bệnh nhân: %s. Chưa có lịch sử khám tại phòng khám.", patientName);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("=== HỒ SƠ BỆNH NHÂN: %s ===\n", patientName));

        // Tiền sử bệnh (nếu có)
        if (patient.getMedicalHistory() != null && !patient.getMedicalHistory().isBlank()) {
            sb.append("Tiền sử bệnh: ").append(patient.getMedicalHistory()).append("\n");
        }

        sb.append("\n--- LỊCH SỬ KHÁM GẦN ĐÂY (mới nhất trước) ---\n");

        int index = 1;
        for (MedicalRecord record : recentRecords.getContent()) {
            sb.append(String.format("\n[Lần %d] Ngày: %s | BS: %s\n",
                    index++,
                    record.getAppointment().getAppointmentTime().toLocalDate(),
                    record.getAppointment().getDoctor().getUser().getFullName()));

            if (record.getSymptoms() != null)
                sb.append("  Triệu chứng: ").append(record.getSymptoms()).append("\n");
            if (record.getDiagnosis() != null)
                sb.append("  Chẩn đoán: ").append(record.getDiagnosis()).append("\n");
            if (record.getTreatmentPlan() != null)
                sb.append("  Phác đồ: ").append(record.getTreatmentPlan()).append("\n");

            // Sinh hiệu
            sb.append(String.format("  Sinh hiệu: Cao=%.1fcm, Nặng=%.1fkg, HA=%s, Nhiệt độ=%.1f°C, Nhịp tim=%dbpm\n",
                    record.getHeight() != null ? record.getHeight() : 0,
                    record.getWeight() != null ? record.getWeight() : 0,
                    record.getBloodPressure() != null ? record.getBloodPressure() : "N/A",
                    record.getTemperature() != null ? record.getTemperature() : 0,
                    record.getHeartRate() != null ? record.getHeartRate() : 0));

            // Đơn thuốc
            if (record.getPrescriptions() != null) {
                for (Prescription presc : record.getPrescriptions()) {
                    if (presc.getPrescriptionDetails() != null && !presc.getPrescriptionDetails().isEmpty()) {
                        sb.append("  Đơn thuốc: ");
                        for (PrescriptionDetail detail : presc.getPrescriptionDetails()) {
                            sb.append(String.format("%s (SL:%d, %s); ",
                                    detail.getDrug().getName(),
                                    detail.getQuantity(),
                                    detail.getDosage() != null ? detail.getDosage() : "theo chỉ định"));
                        }
                        sb.append("\n");
                    }
                    if (presc.getNote() != null && !presc.getNote().isBlank()) {
                        sb.append("  Ghi chú BS: ").append(presc.getNote()).append("\n");
                    }
                }
            }
        }

        log.info("📋 Đã tạo patient context cho BN [{}] với {} lần khám", patientName, recentRecords.getContent().size());
        return sb.toString();
    }
}
