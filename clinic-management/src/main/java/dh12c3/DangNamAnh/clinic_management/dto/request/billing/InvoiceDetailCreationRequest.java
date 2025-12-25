package dh12c3.DangNamAnh.clinic_management.dto.request.billing;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceDetailCreationRequest {
    @NotNull(message = "Invoice ID cannot be empty.")
    Long invoiceId;

    Long serviceId;
    Long drugId;

    @NotNull(message = "Quantity cannot be empty.")
    Integer quantity;

    BigDecimal unitPrice;
}
