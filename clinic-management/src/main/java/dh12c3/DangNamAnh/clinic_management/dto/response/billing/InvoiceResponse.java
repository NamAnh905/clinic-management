package dh12c3.DangNamAnh.clinic_management.dto.response.billing;

import dh12c3.DangNamAnh.clinic_management.enums.PaymentMethod;
import dh12c3.DangNamAnh.clinic_management.enums.PaymentStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceResponse {
    Long invoiceId;
    Long appointmentId;
    String patientName;
    String doctorName;
    BigDecimal totalAmount;
    PaymentStatus paymentStatus;
    PaymentMethod paymentMethod;
    String transactionCode;
}
