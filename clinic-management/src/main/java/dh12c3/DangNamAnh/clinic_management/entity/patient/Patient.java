package dh12c3.DangNamAnh.clinic_management.entity.patient;

import dh12c3.DangNamAnh.clinic_management.entity.appointment.Appointment;
import dh12c3.DangNamAnh.clinic_management.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "Patients")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long patientId;

    @OneToOne
    @JoinColumn(name = "userId", nullable = false, unique = true)
    User user;

    @Column(columnDefinition = "TEXT")
    String medicalHistory;

    @OneToMany(mappedBy = "patient")
    Set<Appointment> appointments;
}

