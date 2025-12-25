package dh12c3.DangNamAnh.clinic_management.dto.request.master;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DrugCreationRequest {
    @NotBlank(message = "Drug name cannot be empty.")
    String name;

    @NotBlank(message = "Unit cannot be empty.")
    String unit;

    String instructions;

    @NotNull(message = "Stock quantity cannot be empty.")
    @Min(0)
    Integer stockQuantity;

    @NotNull(message = "Price cannot be empty.")
    @DecimalMin(value = "0.0")
    BigDecimal price;
}
