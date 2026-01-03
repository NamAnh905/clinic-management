package dh12c3.DangNamAnh.clinic_management.repository.billing;

import dh12c3.DangNamAnh.clinic_management.dto.response.dashboard.ChartDataResponse; // [NEW] Import này quan trọng
import dh12c3.DangNamAnh.clinic_management.entity.billing.InvoiceDetail;
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

    @Query("""
        SELECT new dh12c3.DangNamAnh.clinic_management.dto.response.dashboard.ChartDataResponse(
            CASE WHEN d.service IS NOT NULL THEN 'Dịch vụ' ELSE 'Thuốc' END,
            SUM(d.unitPrice * d.quantity)
        )
        FROM InvoiceDetail d
        JOIN d.invoice i
        WHERE i.paymentStatus = 'PAID'
        AND i.createdAt BETWEEN :startDate AND :endDate
        GROUP BY CASE WHEN d.service IS NOT NULL THEN 'Dịch vụ' ELSE 'Thuốc' END
    """)
    List<dh12c3.DangNamAnh.clinic_management.dto.response.dashboard.ChartDataResponse> getRevenueStructure(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // 2. Top 5 bán chạy (Logic COALESCE tên Dịch vụ hoặc Thuốc)
    // Sửa: d.service.name (ServiceEntity) và d.drug.name (Drug) -> Khớp file bạn gửi
    @Query("""
        SELECT new dh12c3.DangNamAnh.clinic_management.dto.response.dashboard.ChartDataResponse(
            COALESCE(s.name, dr.name),
            SUM(d.unitPrice * d.quantity)
        )
        FROM InvoiceDetail d
        JOIN d.invoice i
        LEFT JOIN d.service s
        LEFT JOIN d.drug dr
        WHERE i.paymentStatus = 'PAID'
        AND i.createdAt BETWEEN :startDate AND :endDate
        GROUP BY COALESCE(s.name, dr.name)
        ORDER BY SUM(d.unitPrice * d.quantity) DESC
    """)
    List<dh12c3.DangNamAnh.clinic_management.dto.response.dashboard.ChartDataResponse> getTopSoldItems(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}