package dh12c3.DangNamAnh.clinic_management.repository.medical;

import dh12c3.DangNamAnh.clinic_management.entity.medical.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {
    @Query(value = """
            SELECT m
            FROM MedicalRecord m
            JOIN FETCH m.appointment a
            JOIN FETCH a.patient p
            JOIN FETCH a.doctor d
            JOIN FETCH p.user u
            WHERE (:doctorId IS NULL OR d.doctorId = :doctorId)
            AND (:startDate IS NULL OR a.appointmentTime >= :startDate)
            AND (:endDate IS NULL OR a.appointmentTime <= :endDate)
            AND (:keyword IS NULL OR :keyword = ''
                OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(m.diagnosis) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """,
    countQuery = """
            SELECT COUNT(m)
            FROM MedicalRecord m
            JOIN m.appointment a
            JOIN a.patient p
            JOIN a.doctor d
            JOIN p.user u
            WHERE (:doctorId IS NULL OR d.doctorId = :doctorId)
            AND (:startDate IS NULL OR a.appointmentTime >= :startDate)
            AND (:endDate IS NULL OR a.appointmentTime <= :endDate)
            AND (:keyword IS NULL OR :keyword = ''
                OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(m.diagnosis) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<MedicalRecord> getAllMedicalRecord(@Param("doctorId") Long doctorId,
                                            @Param("startDate")LocalDateTime startDate,
                                            @Param("endDate")LocalDateTime endDate,
                                            @Param("keyword")String keyword,
                                            Pageable pageable);

    Page<MedicalRecord> findByAppointment_Patient_PatientId(Long patientId, Pageable pageable);

    Optional<MedicalRecord> findByAppointment_AppointmentId(Long appointmentId);
}
