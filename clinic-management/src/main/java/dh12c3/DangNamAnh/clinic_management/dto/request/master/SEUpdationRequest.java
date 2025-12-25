package dh12c3.DangNamAnh.clinic_management.dto.request.master;

import dh12c3.DangNamAnh.clinic_management.enums.ServiceType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class SEUpdationRequest {
    Long serviceId;
    String name;
    BigDecimal price;
    ServiceType type;
}
