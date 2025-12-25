package dh12c3.DangNamAnh.clinic_management.dto.request.medical;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrescriptionCreationRequest {
    @NotNull(message = "RecordId cannot be empty.")
    Long recordId;
    String note;
}
