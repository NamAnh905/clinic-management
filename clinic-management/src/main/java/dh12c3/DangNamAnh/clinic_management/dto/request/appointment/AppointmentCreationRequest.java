package dh12c3.DangNamAnh.clinic_management.dto.request.appointment;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class AppointmentCreationRequest {
    @NotNull(message = "Patient ID cannot be empty.")
    Long patientId;

    @NotNull(message = "Doctor ID cannot be empty.")
    Long doctorId;

    @Future(message = "Appointment time must be strictly in the future.")
    @NotNull(message = "Appointment time cannot be empty.")
    LocalDateTime appointmentTime;

    @NotBlank(message = "Reason cannot be empty.")
    String reason;
}
