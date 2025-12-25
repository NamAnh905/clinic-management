package dh12c3.DangNamAnh.clinic_management.dto.request.appointment;

import dh12c3.DangNamAnh.clinic_management.enums.AppointmentStatus;
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

public class AppointmentUpdationRequest {
    LocalDateTime appointmentTime;
    LocalDateTime endTime;
    String reason;
    AppointmentStatus status;
}
