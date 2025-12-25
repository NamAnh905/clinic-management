package dh12c3.DangNamAnh.clinic_management.dto.response.patient;

import dh12c3.DangNamAnh.clinic_management.dto.response.appointment.AppointmentResponse;
import dh12c3.DangNamAnh.clinic_management.entity.appointment.Appointment;
import dh12c3.DangNamAnh.clinic_management.entity.user.User;
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
public class PatientResponse {
    Long patientId;
    Long userId;
    String patientName;
    String gender;
    String email;
    String phoneNumber;
    LocalDate dateOfBirth;
    String address;
    String medicalHistory;
}
