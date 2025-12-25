package dh12c3.DangNamAnh.clinic_management.repository.billing;

import dh12c3.DangNamAnh.clinic_management.entity.billing.Invoice;
import dh12c3.DangNamAnh.clinic_management.entity.billing.InvoiceDetail;
import dh12c3.DangNamAnh.clinic_management.entity.master.ServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface InvoiceDetailRepository extends JpaRepository<InvoiceDetail, Long> {
    List<InvoiceDetail> findByInvoice_InvoiceId(Long invoiceId);

    @Query("""
        SELECT COALESCE(SUM(d.unitPrice * d.quantity), 0)
        FROM InvoiceDetail d
        WHERE d.invoice.invoiceId = :invoiceId
    """)
    BigDecimal sumTotalByInvoiceId(@Param("invoiceId") Long invoiceId);

    @Query("""
        SELECT COALESCE(SUM(d.unitPrice * d.quantity), 0)
        FROM InvoiceDetail d
        JOIN d.invoice i
        WHERE i.paymentStatus = 'PAID'
        AND i.createdAt BETWEEN :startDate AND :endDate
        AND d.drug IS NOT NULL
    """)
    BigDecimal sumDrugRevenueBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query("""
        SELECT COALESCE(SUM(d.unitPrice * d.quantity), 0)
        FROM InvoiceDetail d
        JOIN d.invoice i
        WHERE i.paymentStatus = 'PAID'
        AND i.createdAt BETWEEN :startDate AND :endDate
        AND d.service IS NOT NULL
    """)
    BigDecimal sumServiceRevenueBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
