package dh12c3.DangNamAnh.clinic_management.dto.response.dashboard;

import dh12c3.DangNamAnh.clinic_management.helper.ExcelColumn;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class RevenueResponse {
    @ExcelColumn(name = "Thời gian", width = 6000)
    String period;

    @ExcelColumn(name = "Tổng doanh thu (VNĐ)", width = 6000)
    BigDecimal totalRevenue;

    @ExcelColumn(name = "Doanh thu thuốc (VNĐ)", width = 6000)
    BigDecimal drugRevenue;

    @ExcelColumn(name = "Doanh thu dịch vụ (VNĐ)", width = 6000)
    BigDecimal serviceRevenue;
}
