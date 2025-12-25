package dh12c3.DangNamAnh.clinic_management.dto.response.staff;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorResponse {
    Long doctorId;
    String fullName;
    Long userId;
    Long specialtyId;
    String phoneNumber;
    String employeeCode;
    String licenseNumber;
    String specialtyName;
}
