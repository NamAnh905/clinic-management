package dh12c3.DangNamAnh.clinic_management.dto.request.appointment;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CancelAppointmentRequest {
    Long appointmentId;
    String phoneNumber;
    String reason;
}