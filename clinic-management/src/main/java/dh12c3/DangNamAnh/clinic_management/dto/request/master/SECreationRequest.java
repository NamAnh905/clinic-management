package dh12c3.DangNamAnh.clinic_management.dto.request.master;

import dh12c3.DangNamAnh.clinic_management.enums.ServiceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class SECreationRequest {
    @NotBlank(message = "Service name cannot be empty.")
    String name;

    @NotNull(message = "Price cannot be empty.")
    @DecimalMin(value = "0.0")
    BigDecimal price;

    @NotNull(message = "Service type cannot be empty.")
    ServiceType type;
}
