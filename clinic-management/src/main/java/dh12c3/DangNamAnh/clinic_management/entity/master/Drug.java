package dh12c3.DangNamAnh.clinic_management.entity.master;

import dh12c3.DangNamAnh.clinic_management.entity.medical.PrescriptionDetail;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Set;

@Entity
@Table(name = "Drugs")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Drug {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long drugId;

    @Column(nullable = false, unique = true, length = 100)
    String name;

    @Column(nullable = false, length = 50)
    String unit;

    @Column(columnDefinition = "TEXT")
    String instructions;

    @Column(nullable = false)
    Integer stockQuantity;

    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal price;

    boolean deleted = false;

    @OneToMany(mappedBy = "drug")
    Set<PrescriptionDetail> prescriptionDetails;
}