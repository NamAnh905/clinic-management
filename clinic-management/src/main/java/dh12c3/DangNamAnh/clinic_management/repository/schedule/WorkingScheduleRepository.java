package dh12c3.DangNamAnh.clinic_management.repository.schedule;

import dh12c3.DangNamAnh.clinic_management.entity.schedule.WorkingSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;

public interface WorkingScheduleRepository extends JpaRepository<WorkingSchedule,Long> {
    @Query(value = """
        SELECT w
        FROM WorkingSchedule w
        LEFT JOIN FETCH w.doctor d
        LEFT JOIN FETCH d.user u
        LEFT JOIN FETCH d.specialty ds
        LEFT JOIN FETCH w.receptionist r
        WHERE (w.deleted = false)
        AND (:specialtyId IS NULL OR ds.specialtyId = :specialtyId)
        AND (:doctorId IS NULL OR d.doctorId = :doctorId)
        AND (:receptionistId IS NULL OR r.receptionistId = :receptionistId)
        AND w.workDate >= :startDate AND w.workDate <= :endDate
        AND (
            :viewType IS NULL
            OR (:viewType = 'DOCTOR' AND d.doctorId IS NOT NULL)
            OR (:viewType = 'RECEPTIONIST' AND r.receptionistId IS NOT NULL)
        )
        """,
            countQuery = """
        SELECT COUNT(w)
        FROM WorkingSchedule w
        LEFT JOIN w.doctor d
        LEFT JOIN d.specialty ds
        LEFT JOIN w.receptionist r
        WHERE (w.deleted = false)
        AND (:specialtyId IS NULL OR ds.specialtyId = :specialtyId)
        AND (:doctorId IS NULL OR d.doctorId = :doctorId)
        AND (:receptionistId IS NULL OR r.receptionistId = :receptionistId)
        AND w.workDate >= :startDate AND w.workDate <= :endDate
        AND (
            :viewType IS NULL
            OR (:viewType = 'DOCTOR' AND d.doctorId IS NOT NULL)
            OR (:viewType = 'RECEPTIONIST' AND r.receptionistId IS NOT NULL)
        )
        """)
    Page<WorkingSchedule> getAllWorkingSchedules(@Param("doctorId") Long doctorId,
                                                 @Param("receptionistId") Long receptionistId,
                                                 @Param("specialtyId") Long specialtyId,
                                                 @Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate,
                                                 @Param("viewType") String viewType,
                                                 Pageable pageable);

    @Query("""
            SELECT COUNT(w)
            FROM WorkingSchedule w
            LEFT JOIN w.doctor d
            LEFT JOIN w.receptionist r
            WHERE w.deleted = false
            AND w.workDate = :workDate
            AND (w.startTime < :endTime AND w.endTime > :startTime)
            AND (
                (:doctorId IS NOT NULL AND d.doctorId = :doctorId) OR
                (:receptionistId IS NOT NULL AND r.receptionistId = :receptionistId)
            )
            """)
    Long existsByOverLap(
            @Param("doctorId") Long doctorId,
            @Param("receptionistId") Long receptionistId,
            @Param("workDate") LocalDate workDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime
    );

    @Query("""
            SELECT COUNT(w)
            FROM WorkingSchedule w
            LEFT JOIN w.doctor d
            LEFT JOIN w.receptionist r
            WHERE w.deleted = false
            AND w.workDate = :workDate
            AND (w.startTime < :endTime AND w.endTime > :startTime)
            AND w.scheduleId != :scheduleId
            AND (
                (:doctorId IS NOT NULL AND d.doctorId = :doctorId) OR
                (:receptionistId IS NOT NULL AND r.receptionistId = :receptionistId)
            )
            """)
    Long existsByOverLapForUpdate(
            @Param("doctorId") Long doctorId,
            @Param("receptionistId") Long receptionistId,
            @Param("workDate") LocalDate workDate,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("scheduleId") Long scheduleId
    );
}
