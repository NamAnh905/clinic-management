package dh12c3.DangNamAnh.clinic_management.entity.medical;

import dh12c3.DangNamAnh.clinic_management.entity.master.Drug;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@Table(name = "PrescriptionDetails")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PrescriptionDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long detailId;

    @ManyToOne
    @JoinColumn(name = "prescriptionId", nullable = false)
    Prescription prescription;

    @ManyToOne
    @JoinColumn(name = "drugId", nullable = false)
    Drug drug;

    @Column(nullable = false)
    Integer quantity;

    @Column(length = 255)
    String dosage;
}

