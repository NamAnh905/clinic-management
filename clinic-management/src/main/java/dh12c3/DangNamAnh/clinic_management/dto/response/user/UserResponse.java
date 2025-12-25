package dh12c3.DangNamAnh.clinic_management.dto.response.user;

import dh12c3.DangNamAnh.clinic_management.enums.Gender;
import dh12c3.DangNamAnh.clinic_management.helper.ExcelColumn;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserResponse {
    @ExcelColumn(name = "ID")
    Long userId;

    @ExcelColumn(name = "Họ và tên")
    String fullName;

    @ExcelColumn(name = "Email")
    String email;

    @ExcelColumn(name = "Số điện thoại")
    String phoneNumber;

    @ExcelColumn(name = "Giới tính")
    Gender gender;

    @ExcelColumn(name = "Ngày sinh")
    LocalDate dateOfBirth;

    @ExcelColumn(name = "Địa chỉ")
    String address;

    Set<RoleResponse> roles;

    @ExcelColumn(name = "Trạng thái hoạt động")
    Boolean isActive;

    @ExcelColumn(name = "Vai trò")
    public String getRolesForExcel() {
        if (roles == null || roles.isEmpty()) {
            return "";
        }
        return roles.stream()
                .map(RoleResponse::getName)
                .collect(Collectors.joining(", "));
    }
}