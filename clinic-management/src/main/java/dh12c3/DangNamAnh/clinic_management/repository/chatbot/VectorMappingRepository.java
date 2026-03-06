package dh12c3.DangNamAnh.clinic_management.repository.chatbot;

import dh12c3.DangNamAnh.clinic_management.entity.chatbot.VectorMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VectorMappingRepository extends JpaRepository<VectorMapping, Long> {
    Optional<VectorMapping> findByEntityTypeAndEntityId(String entityType, Long entityId);
    void deleteByEntityTypeAndEntityId(String entityType, Long entityId);
}