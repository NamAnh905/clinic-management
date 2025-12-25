package dh12c3.DangNamAnh.clinic_management.dto.request.billing;

import dh12c3.DangNamAnh.clinic_management.enums.PaymentMethod;
import dh12c3.DangNamAnh.clinic_management.enums.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceCreationRequest {

    @NotNull(message = "AppointmentId cannot be null.")
    Long appointmentId;

    BigDecimal totalAmount;

    @NotNull(message = "Payment status cannot be null.")
    PaymentStatus paymentStatus;

    @NotNull(message = "Payment method cannot be null.")
    PaymentMethod paymentMethod;

    String transactionCode;

    List<Long> serviceIds;
}
