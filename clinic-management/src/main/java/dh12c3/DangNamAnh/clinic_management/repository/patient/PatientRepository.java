package dh12c3.DangNamAnh.clinic_management.repository.patient;

import dh12c3.DangNamAnh.clinic_management.entity.patient.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    @Query(value = """
            SELECT p
            FROM Patient p
            JOIN FETCH p.user u
            WHERE u.isActive = true
            AND (:keyword IS NULL OR :keyword = ''
                    OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(u.address) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """,
    countQuery = """
            SELECT COUNT(p)
            FROM Patient p
            JOIN p.user u
            WHERE u.isActive = true
            AND (:keyword IS NULL OR :keyword = ''
                    OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(u.address) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Patient> searchPatients(@Param("keyword") String keyword, Pageable pageable);

    Optional<Patient> findByUser_Email(String email);

    Optional<Patient> findByUser_UserId(Long userId);
}
