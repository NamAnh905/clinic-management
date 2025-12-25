package dh12c3.DangNamAnh.clinic_management.dto.request.medical;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class PresDetailCreationRequest {
    @NotNull(message = "PrescriptionId cannot be empty.")
    Long prescriptionId;

    @NotNull(message = "DrugId cannot be empty.")
    Long drugId;

    @NotNull(message = "Quantity cannot be empty.")
    @Positive(message = "Please enter a non-negative quantity value.")
    Integer quantity;

    @NotBlank(message = "Dosage cannot be empty.")
    String dosage;
}
