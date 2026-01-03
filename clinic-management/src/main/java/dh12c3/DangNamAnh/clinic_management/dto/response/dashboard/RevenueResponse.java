package dh12c3.DangNamAnh.clinic_management.dto.response.dashboard;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class RevenueResponse {
    private String period;
    private BigDecimal totalRevenue;
    private BigDecimal drugRevenue;
    private BigDecimal serviceRevenue;
}
