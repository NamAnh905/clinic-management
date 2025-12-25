package dh12c3.DangNamAnh.clinic_management.dto.request.staff;

import dh12c3.DangNamAnh.clinic_management.entity.appointment.Appointment;
import dh12c3.DangNamAnh.clinic_management.entity.master.Specialty;
import dh12c3.DangNamAnh.clinic_management.entity.schedule.WorkingSchedule;
import dh12c3.DangNamAnh.clinic_management.entity.user.User;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;


import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class DoctorCreationRequest {
    @NotNull(message = "UserId cannot be empty.")
    Long userId;

    @NotNull(message = "SpecialtyId cannot be empty.")
    Long specialtyId;

    String employeeCode;
    String licenseNumber;
}
