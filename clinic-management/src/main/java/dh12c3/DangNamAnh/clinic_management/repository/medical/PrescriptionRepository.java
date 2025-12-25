package dh12c3.DangNamAnh.clinic_management.repository.medical;

import dh12c3.DangNamAnh.clinic_management.entity.medical.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    @Query("""
            SELECT p
            FROM Prescription p
            JOIN FETCH p.medicalRecord m
            JOIN FETCH m.appointment a
            JOIN FETCH a.doctor d
            JOIN FETCH a.patient pat
            LEFT JOIN FETCH pat.user u
            WHERE (:doctorId IS NULL OR d.doctorId = :doctorId)
    """)
    Page<Prescription> getAllPrescription(@Param("doctorId") Long doctorId, Pageable pageable);

    Page<Prescription> findByMedicalRecord_Appointment_Patient_PatientId(Long patientId, Pageable pageable);

    Optional<Prescription> findByMedicalRecord_RecordId(Long recordId);

    Optional<Prescription> findByMedicalRecord_Appointment_AppointmentId(Long appointmentId);
}
