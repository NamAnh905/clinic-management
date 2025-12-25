package dh12c3.DangNamAnh.clinic_management.dto.response.master;

import dh12c3.DangNamAnh.clinic_management.dto.response.medical.PresDetailResponse;
import dh12c3.DangNamAnh.clinic_management.entity.medical.PrescriptionDetail;
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
public class DrugResponse {
    @ExcelColumn(name = "ID")
    Long drugId;

    @ExcelColumn(name = "Tên thuốc")
    String name;

    @ExcelColumn(name = "Đơn vị")
    String unit;

    @ExcelColumn(name = "Hướng dẫn sử dụng")
    String instructions;

    @ExcelColumn(name = "Tồn kho")
    Integer stockQuantity;

    @ExcelColumn(name = "Đơn giá (VNĐ)")
    BigDecimal price;
}
