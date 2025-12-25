package dh12c3.DangNamAnh.clinic_management.entity.medical;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@Table(name = "Prescriptions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long prescriptionId;

    @ManyToOne
    @JoinColumn(name = "recordId", nullable = false)
    MedicalRecord medicalRecord;

    @Column(columnDefinition = "TEXT")
    String note;

    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<PrescriptionDetail> prescriptionDetails;
}
