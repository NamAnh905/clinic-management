package dh12c3.DangNamAnh.clinic_management.repository.staff;

import dh12c3.DangNamAnh.clinic_management.entity.staff.Receptionist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ReceptionistRepository extends JpaRepository<Receptionist, Long> {
    @Query(value = """
            SELECT r
            FROM Receptionist r
            JOIN FETCH r.user u
            WHERE u.isActive = true
            AND (:keyword IS NULL OR :keyword = ''
            OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(r.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """,
    countQuery = """
            SELECT r
            FROM Receptionist r
            JOIN r.user u
            WHERE u.isActive = true
            AND (:keyword IS NULL OR :keyword = ''
            OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(r.employeeCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Receptionist> getAllReceptionistsWithDetails(@Param("keyword") String keyword, Pageable pageable);

    boolean existsByEmployeeCode(String employeeCode);

    Optional<Receptionist> findByUser_UserId(Long userId);
}
