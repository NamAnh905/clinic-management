package dh12c3.DangNamAnh.clinic_management.dto.request.user;

import dh12c3.DangNamAnh.clinic_management.enums.Gender;
import dh12c3.DangNamAnh.clinic_management.helper.AppConstants;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCreationRequest {
    @NotBlank(message = "Full name cannot be empty.")
    @Size(min = 5, max = 50, message = "You name must be at least 5 characters.")
    String fullName;

    @NotBlank(message = "Email cannot be empty.")
    @Email(message = "Invalid email format.")
    @Size(min = 5, max = 100, message = "Email must be between 5 and 100 characters.")
    String email;

    @NotBlank(message = "Password cannot be empty.")
    @Size(min = 8, message = "Password must be at least 8 characters")
    String password;

    @Pattern(regexp = AppConstants.PHONE_REGEX, message = "Phone number must be exactly 10 digits and start with 0")
    String phoneNumber;

    String address;

    @NotNull(message = "Gender cannot be empty.")
    Gender gender;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    LocalDate dateOfBirth;
}
