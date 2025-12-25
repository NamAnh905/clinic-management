package dh12c3.DangNamAnh.clinic_management.dto.response.medical;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MedicalRecordResponse {
    Long recordId;
    Long appointmentId;
    String patientName;
    Double height;
    Double weight;
    String bloodPressure;
    Double temperature;
    Integer heartRate;
    String diagnosis;
    String symptoms;
    String treatmentPlan;
    LocalDateTime visitDate;
    Long doctorId;
    String doctorName;
}
