package dh12c3.DangNamAnh.clinic_management.repository.master;

import dh12c3.DangNamAnh.clinic_management.entity.master.Drug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DrugRepository extends JpaRepository<Drug, Long> {
    @Query(value = """
            SELECT d
            FROM Drug d
            WHERE d.deleted = false
            AND (:keyword IS NULL OR :keyword = ''
                    OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """,
            countQuery = """
            SELECT COUNT(d)
            FROM Drug d
            WHERE d.deleted = false
            AND (:keyword IS NULL OR :keyword = ''
                    OR LOWER(d.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Drug> getAllDrugs(@Param("keyword") String keyword, Pageable pageable);
}
