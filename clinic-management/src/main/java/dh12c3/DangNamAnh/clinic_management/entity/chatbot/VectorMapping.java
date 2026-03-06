package dh12c3.DangNamAnh.clinic_management.entity.chatbot;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vector_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VectorMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type")
    private String entityType; // "drug", "doctor", "service"...

    @Column(name = "entity_id")
    private Long entityId;     // ID trong MySQL của bảng gốc

    @Column(name = "qdrant_id")
    private String qdrantId;   // ID do Qdrant tự sinh ra
}