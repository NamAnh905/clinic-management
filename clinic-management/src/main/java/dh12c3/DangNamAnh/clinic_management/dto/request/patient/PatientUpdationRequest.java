package dh12c3.DangNamAnh.clinic_management.dto.request.patient;

import dh12c3.DangNamAnh.clinic_management.enums.Gender;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PatientUpdationRequest {
    String fullName;
    String phoneNumber;
    Gender gender;
    LocalDate dateOfBirth;
    String address;
    String medicalHistory;
}
