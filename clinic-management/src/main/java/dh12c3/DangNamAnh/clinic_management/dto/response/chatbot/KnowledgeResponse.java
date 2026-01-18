package dh12c3.DangNamAnh.clinic_management.dto.response.chatbot;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class KnowledgeResponse {
    Long id;
    String question;
    String answer;
    String type;
    Boolean isActive;
}