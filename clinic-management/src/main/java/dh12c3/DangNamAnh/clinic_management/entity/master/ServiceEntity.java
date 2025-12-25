package dh12c3.DangNamAnh.clinic_management.entity.master;

import dh12c3.DangNamAnh.clinic_management.entity.billing.InvoiceDetail;
import dh12c3.DangNamAnh.clinic_management.enums.ServiceType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.Set;

@Entity
@Table(name = "Services")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ServiceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long serviceId;

    @Column(nullable = false, unique = true, length = 100)
    String name;

    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal price;

    @Enumerated(EnumType.STRING)
    ServiceType type;

    boolean deleted = false;

    @OneToMany(mappedBy = "service")
    Set<InvoiceDetail> invoiceDetails;
}
