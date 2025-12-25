package dh12c3.DangNamAnh.clinic_management.dto.response.medical;

import dh12c3.DangNamAnh.clinic_management.entity.medical.PrescriptionDetail;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrescriptionResponse {
    Long prescriptionId;
    Long recordId;
    String doctorName;
    String note;
}
