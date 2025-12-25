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
    private String period;          // Ví dụ: "12/2023" hoặc "01/01/2023 - 31/01/2023"
    private BigDecimal totalRevenue;   // Tổng doanh thu (Khớp với Invoice)
    private BigDecimal drugRevenue;    // Doanh thu bán thuốc
    private BigDecimal serviceRevenue; // Doanh thu dịch vụ
}
