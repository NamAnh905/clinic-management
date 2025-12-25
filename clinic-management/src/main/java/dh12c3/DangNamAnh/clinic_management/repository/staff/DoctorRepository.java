package dh12c3.DangNamAnh.clinic_management.repository.staff;

import dh12c3.DangNamAnh.clinic_management.entity.staff.Doctor;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    @Query(value = """
            SELECT d
            FROM Doctor d
            JOIN FETCH d.user u
            JOIN FETCH d.specialty s
            WHERE u.isActive = true
            AND (:keyword IS NULL OR :keyword = ''
                    OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """,
    countQuery = """
            SELECT COUNT(d)
            FROM Doctor d
            JOIN d.user u
            JOIN d.specialty s
            WHERE u.isActive = true
            AND (:keyword IS NULL OR :keyword = ''
                    OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                    OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Doctor> findAllDoctorsWithDetails(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT d
            FROM Doctor d
            JOIN FETCH d.user u
            JOIN FETCH d.specialty s
            WHERE u.isActive = true
            AND d.specialty.specialtyId = :specialtyId
            """)
    Page<Doctor> findBySpecialtyIdWithDetails(@Param("specialtyId") Long specialtyId, Pageable pageable);

    Optional<Doctor> findByUser_Email(String email);

    Optional<Doctor> findByUser_UserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Doctor d WHERE d.user.isActive = true AND d.user.userId = :userId")
    Optional<Doctor> findByUserIdWithLock(@Param("userId") Long userId);

    boolean existsByEmployeeCode(String employeeCode);
    boolean existsByLicenseNumber(String licenseNumber);
}
