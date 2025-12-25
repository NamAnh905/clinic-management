package dh12c3.DangNamAnh.clinic_management.dto.request.medical;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrescriptionUpdateRequest {
    String note;
}
