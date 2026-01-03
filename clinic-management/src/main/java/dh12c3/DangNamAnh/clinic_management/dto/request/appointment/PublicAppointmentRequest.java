package dh12c3.DangNamAnh.clinic_management.dto.request.appointment;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PublicAppointmentRequest {
    @NotNull(message = "Name is required.")
    String fullName;

    @NotNull(message = "Phone number is required.")
    String phoneNumber;

    String email;

    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    LocalDate dateOfBirth;
    String gender;
    String address;

    @NotNull(message = "Doctor is required.")
    Long doctorId;

    @NotNull(message = "Appointment time is required.")
    LocalDateTime appointmentTime;

    @NotNull(message = "Reason is required.")
    String reason;
}