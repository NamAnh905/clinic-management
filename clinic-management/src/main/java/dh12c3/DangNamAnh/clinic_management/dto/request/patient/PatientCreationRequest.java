package dh12c3.DangNamAnh.clinic_management.dto.request.patient;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class PatientCreationRequest {
    @NotNull(message = "User ID cannot be empty.")
    Long userId;
    String medicalHistory;
}
