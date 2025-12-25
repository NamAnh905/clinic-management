package dh12c3.DangNamAnh.clinic_management.dto.request.staff;

import dh12c3.DangNamAnh.clinic_management.entity.appointment.Appointment;
import dh12c3.DangNamAnh.clinic_management.entity.schedule.WorkingSchedule;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DoctorUpdateRequest {
    Long specialtyId;
    String employeeCode;
    String licenseNumber;
}
