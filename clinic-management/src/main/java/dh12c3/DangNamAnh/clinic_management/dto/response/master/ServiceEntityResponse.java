package dh12c3.DangNamAnh.clinic_management.dto.response.master;

import dh12c3.DangNamAnh.clinic_management.enums.ServiceType;
import dh12c3.DangNamAnh.clinic_management.helper.ExcelColumn;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class ServiceEntityResponse {
    @ExcelColumn(name = "Mã dịch vụ")
    Long serviceId;

    @ExcelColumn(name = "Tên dịch vụ")
    String name;

    @ExcelColumn(name = "Loại dịch vụ")
    ServiceType type;

    @ExcelColumn(name = "Giá")
    BigDecimal price;
}
