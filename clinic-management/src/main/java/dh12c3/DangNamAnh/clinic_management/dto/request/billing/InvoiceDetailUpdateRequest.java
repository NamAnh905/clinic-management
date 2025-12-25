package dh12c3.DangNamAnh.clinic_management.dto.request.billing;

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
public class InvoiceDetailUpdateRequest {
    Integer quantity;
    BigDecimal unitPrice;
}
