package dh12c3.DangNamAnh.clinic_management.repository.billing;

import dh12c3.DangNamAnh.clinic_management.entity.billing.Invoice;
import dh12c3.DangNamAnh.clinic_management.enums.PaymentMethod;
import dh12c3.DangNamAnh.clinic_management.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    @Query(value = """
            SELECT i
            FROM Invoice i
            JOIN FETCH i.appointment a
            JOIN FETCH a.patient p
            JOIN FETCH p.user pu
            JOIN FETCH a.doctor d
            JOIN FETCH d.user du
            WHERE (:paymentStatus IS NULL OR i.paymentStatus = :paymentStatus)
            AND (:paymentMethod IS NULL OR i.paymentMethod = :paymentMethod)
            AND (:startDate IS NULL OR i.createdAt >= :startDate)
            AND (:endDate IS NULL OR i.createdAt <= :endDate)
            AND (:keyword IS NULL OR :keyword = ''
                OR LOWER(pu.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(i.transactionCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """,
    countQuery = """
            SELECT COUNT(i)
            FROM Invoice i
            JOIN i.appointment a
            JOIN a.patient p
            JOIN p.user pu
            JOIN a.doctor d
            JOIN d.user du
            WHERE (:paymentStatus IS NULL OR i.paymentStatus =:paymentStatus)
            AND (:paymentMethod IS NULL OR i.paymentMethod =:paymentMethod)
            AND (:startDate IS NULL OR i.createdAt >= :startDate)
            AND (:endDate IS NULL OR i.createdAt <= :endDate)
            AND (:keyword IS NULL OR :keyword = ''
                OR LOWER(pu.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(i.transactionCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Invoice> getAllInvoiceDetails(@Param("paymentStatus") PaymentStatus paymentStatus,
                                       @Param("paymentMethod") PaymentMethod paymentMethod,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate,
                                       @Param("keyword") String keyword,
                                       Pageable pageable);

    Page<Invoice> findByAppointment_Patient_PatientId(Long patientId, Pageable pageable);

    Optional<Invoice> findByAppointment_AppointmentId(Long appointmentId);

    boolean existsByAppointment_AppointmentId(Long appointmentId);

    @Query("""
        SELECT COALESCE(SUM(i.totalAmount), 0)
        FROM Invoice i
        WHERE i.paymentStatus = 'PAID'
        AND i.createdAt BETWEEN :startDate AND :endDate
    """)
    BigDecimal sumTotalRevenueBetween(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
