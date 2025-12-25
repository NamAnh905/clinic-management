package dh12c3.DangNamAnh.clinic_management.dto.request.billing;

import dh12c3.DangNamAnh.clinic_management.enums.PaymentMethod;
import dh12c3.DangNamAnh.clinic_management.enums.PaymentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceUpdateRequest {
    BigDecimal totalAmount;
    PaymentStatus paymentStatus;
    PaymentMethod paymentMethod;
    String transactionCode;
    List<Long> serviceIds;
}
