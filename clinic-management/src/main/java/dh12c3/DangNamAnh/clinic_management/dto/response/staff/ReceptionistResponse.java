package dh12c3.DangNamAnh.clinic_management.dto.response.staff;

import dh12c3.DangNamAnh.clinic_management.enums.Gender;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class ReceptionistResponse {
    Long receptionistId;
    String employeeCode;
    String fullName;
    String email;
    String phoneNumber;
    Gender gender;
    LocalDate dateOfBirth;
    LocalDate hireDate;
}
