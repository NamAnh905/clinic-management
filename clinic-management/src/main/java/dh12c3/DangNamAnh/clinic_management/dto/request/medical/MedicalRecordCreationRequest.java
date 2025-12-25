package dh12c3.DangNamAnh.clinic_management.dto.request.medical;

import dh12c3.DangNamAnh.clinic_management.entity.appointment.Appointment;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class MedicalRecordCreationRequest {
    @NotNull(message = "AppointmentId cannot be empty.")
    Long appointmentId;

    @NotNull(message = "Height cannot be empty.")
    @Positive(message = "Please enter a non-negative height value.")
    Double height;

    @NotNull(message = "Weight cannot be empty.")
    @Positive(message = "Please enter a non-negative weight value.")
    Double weight;

    String bloodPressure;
    Double temperature;

    @Positive(message = "Please enter a non-negative heart rate value.")
    Integer heartRate;

    @NotBlank(message = "Diagnosis cannot be empty.")
    String diagnosis;
    String symptoms;
    String treatmentPlan;
    String testResults;
//    List<Long> serviceIds;
}
