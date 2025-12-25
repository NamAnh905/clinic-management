package dh12c3.DangNamAnh.clinic_management.entity.appointment;

import dh12c3.DangNamAnh.clinic_management.enums.AppointmentStatus;
import dh12c3.DangNamAnh.clinic_management.entity.billing.Invoice;
import dh12c3.DangNamAnh.clinic_management.entity.medical.MedicalRecord;
import dh12c3.DangNamAnh.clinic_management.entity.patient.Patient;
import dh12c3.DangNamAnh.clinic_management.entity.staff.Doctor;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "Appointments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long appointmentId;

    @ManyToOne
    @JoinColumn(name = "patientId", nullable = false)
    Patient patient;

    @ManyToOne
    @JoinColumn(name = "doctorId", nullable = false)
    Doctor doctor;

    @Column(nullable = false)
    LocalDateTime appointmentTime;

    @Column(nullable = false)
    LocalDateTime endTime;

    @Column(length = 255)
    String reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    AppointmentStatus status;

    boolean deleted = false;

    @OneToOne(mappedBy = "appointment")
    MedicalRecord medicalRecord;

    @OneToOne(mappedBy = "appointment")
    Invoice invoice;
}

