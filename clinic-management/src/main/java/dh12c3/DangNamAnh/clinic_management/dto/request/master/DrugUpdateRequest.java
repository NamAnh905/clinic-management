package dh12c3.DangNamAnh.clinic_management.dto.request.master;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DrugUpdateRequest {
    Long drugId;
    String name;
    String unit;
    String instructions;
    Integer stockQuantity;
    BigDecimal price;
}
