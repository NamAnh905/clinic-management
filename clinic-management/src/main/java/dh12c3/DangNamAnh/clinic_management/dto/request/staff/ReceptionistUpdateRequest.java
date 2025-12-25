package dh12c3.DangNamAnh.clinic_management.dto.request.staff;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class ReceptionistUpdateRequest {
    String employeeCode;
    LocalDate hireDate;
}
