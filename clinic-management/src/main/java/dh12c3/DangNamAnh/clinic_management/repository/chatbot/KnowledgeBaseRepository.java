package dh12c3.DangNamAnh.clinic_management.repository.chatbot;

import dh12c3.DangNamAnh.clinic_management.entity.chatbot.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {
    List<KnowledgeBase> findByIsActiveTrue();
}