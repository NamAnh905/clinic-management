package dh12c3.DangNamAnh.clinic_management.entity.billing;

import dh12c3.DangNamAnh.clinic_management.entity.master.Drug;
import dh12c3.DangNamAnh.clinic_management.entity.master.ServiceEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Table(name = "InvoiceDetails")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long invoiceDetailId;

    @ManyToOne
    @JoinColumn(name = "invoiceId", nullable = false)
    Invoice invoice;

    @ManyToOne
    @JoinColumn(name = "serviceId", nullable = true)
    ServiceEntity service;

    @ManyToOne
    @JoinColumn(name = "drugId", nullable = true)
    Drug drug;

    @Column(nullable = false)
    Integer quantity;

    @Column(nullable = false, precision = 15, scale = 2)
    BigDecimal unitPrice;
}
