package dh12c3.DangNamAnh.clinic_management.dto.response.staff;

import dh12c3.DangNamAnh.clinic_management.enums.Gender;
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
    Gender gender;
    Long userId;
    Long specialtyId;
    String phoneNumber;
    String employeeCode;
    String licenseNumber;
    String specialtyName;
    String image;
}
