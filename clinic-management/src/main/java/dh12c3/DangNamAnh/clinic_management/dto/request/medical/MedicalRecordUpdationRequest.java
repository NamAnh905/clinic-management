package dh12c3.DangNamAnh.clinic_management.dto.request.medical;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MedicalRecordUpdationRequest {
    String diagnosis;
    Double height;
    Double weight;
    String bloodPressure;
    Double temperature;
    Integer heartRate;
    String symptoms;
    String treatmentPlan;
}
