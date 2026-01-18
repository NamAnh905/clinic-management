package dh12c3.DangNamAnh.clinic_management.dto.request.chatbot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class KnowledgeRequest {
    private String question;
    private String answer;
    private String type;
}
