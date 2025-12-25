package dh12c3.DangNamAnh.clinic_management.repository.master;

import dh12c3.DangNamAnh.clinic_management.entity.master.ServiceEntity;
import dh12c3.DangNamAnh.clinic_management.enums.ServiceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ServiceEntityRepository extends JpaRepository<ServiceEntity, Long> {
    @Query(value = """
            SELECT s
            FROM ServiceEntity s
            WHERE s.deleted = false
            AND (:type IS NULL OR s.type = :type)
            AND (:keyword IS NULL OR :keyword = ''
                    OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """,
            countQuery = """
            SELECT COUNT(s)
            FROM ServiceEntity s
            WHERE s.deleted = false
            AND (:type IS NULL OR s.type = :type)
            AND (:keyword IS NULL OR :keyword = ''
                    OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<ServiceEntity> getAllServiceEntities(@Param("type")ServiceType type,
                                              @Param("keyword") String keyword,
                                              Pageable pageable);
}
