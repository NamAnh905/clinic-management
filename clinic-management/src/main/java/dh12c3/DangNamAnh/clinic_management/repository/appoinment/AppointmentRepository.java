package dh12c3.DangNamAnh.clinic_management.repository.appoinment;

import dh12c3.DangNamAnh.clinic_management.entity.appointment.Appointment;
import dh12c3.DangNamAnh.clinic_management.enums.AppointmentStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
   @Query(value = """
           SELECT a
           FROM Appointment a
           JOIN FETCH a.doctor d
           JOIN FETCH d.user du
           JOIN FETCH a.patient p
           JOIN FETCH p.user pu
           WHERE (:doctorId IS NULL OR du.userId = :doctorId)
           AND (:patientId IS NULL OR pu.userId = :patientId)
           AND (:status IS NULL OR a.status = :status)
           AND (:startDate IS NULL OR a.appointmentTime >= :startDate)
           AND (:endDate IS NULL OR a.appointmentTime <= :endDate)
           AND (:keyword IS NULL OR :keyword = ''
                OR LOWER(du.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(pu.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
           """,
   countQuery = """
           SELECT COUNT(a)
           FROM Appointment a
           JOIN a.doctor d
           JOIN d.user du
           JOIN a.patient p
           JOIN p.user pu
           WHERE (:doctorId IS NULL OR du.userId = :doctorId)
           AND (:patientId IS NULL OR pu.userId = :patientId)
           AND (:status IS NULL OR a.status = :status)
           AND (:startDate IS NULL OR a.appointmentTime >= :startDate)
           AND (:endDate IS NULL OR a.appointmentTime <= :endDate)
           AND (:keyword IS NULL OR :keyword = ''
                OR LOWER(du.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(pu.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
           """)
   Page<Appointment> searchAppointments(@Param("doctorId") Long doctorId,
                                        @Param("patientId") Long patientId,
                                        @Param("status") AppointmentStatus status,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate,
                                        @Param("keyword") String keyword,
                                        Pageable pageable);

    @Query("""
        SELECT COUNT(a) > 0
        FROM Appointment a
        WHERE a.doctor.doctorId = :doctorId
        AND a.deleted = false
        AND a.status IN :statuses
        AND (a.appointmentTime < :newEndTime AND a.endTime > :newStartTime)
    """)
    boolean existsByOverlap(
            @Param("doctorId") Long doctorId,
            @Param("newStartTime") LocalDateTime newStartTime,
            @Param("newEndTime") LocalDateTime newEndTime,
            @Param("statuses") List<AppointmentStatus> statuses
    );

    boolean existsByPatient_PatientId(Long patientId);

    @Query("""
        SELECT COUNT(a) > 0
        FROM Appointment a
        WHERE a.doctor.doctorId = :doctorId
        AND a.appointmentTime >= :startTime
        AND a.appointmentTime < :endTime
        AND a.status IN :statuses
    """)
    boolean existsActiveAppointment(
            @Param("doctorId") Long doctorId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("statuses") List<AppointmentStatus> statuses
    );
}
