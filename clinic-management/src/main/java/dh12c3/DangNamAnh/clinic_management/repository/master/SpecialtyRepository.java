package dh12c3.DangNamAnh.clinic_management.repository.master;

import dh12c3.DangNamAnh.clinic_management.dto.response.master.SpecialtyResponse;
import dh12c3.DangNamAnh.clinic_management.entity.master.Specialty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

    @Query(value = """
            SELECT s
            FROM Specialty s
            WHERE s.deleted = false
            AND (:keyword IS NULL OR :keyword = ''
                    OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """,
            countQuery = """
            SELECT COUNT(s)
            FROM Specialty s
            WHERE s.deleted = false
            AND (:keyword IS NULL OR :keyword = ''
                    OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Specialty> getAllSpecialties(@Param("keyword")String keyword, Pageable pageable);
}
