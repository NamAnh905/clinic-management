package dh12c3.DangNamAnh.clinic_management.entity.medical;

import dh12c3.DangNamAnh.clinic_management.entity.appointment.Appointment;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@Table(name = "MedicalRecords")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MedicalRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long recordId;

    @OneToOne
    @JoinColumn(name = "appointmentId", nullable = false, unique = true)
    Appointment appointment;

    @Column(columnDefinition = "TEXT")
    String diagnosis;

    @Column(columnDefinition = "TEXT")
    String symptoms;

    @Column(columnDefinition = "TEXT")
    String treatmentPlan;

    Double height;
    Double weight;
    String bloodPressure;
    Double temperature;
    Integer heartRate;

    @Column(columnDefinition = "TEXT")
    String testResults;

    @OneToMany(mappedBy = "medicalRecord")
    Set<Prescription> prescriptions;
}

