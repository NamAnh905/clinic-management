package dh12c3.DangNamAnh.clinic_management.dto.request.user;

import dh12c3.DangNamAnh.clinic_management.enums.Gender;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserUpdateRequest {
    String fullName;
    String oldPassword;
    String password;
    String phoneNumber;
    Gender gender;
    LocalDate dateOfBirth;
    String address;
    List<String> roles;
    Boolean isActive;
}
