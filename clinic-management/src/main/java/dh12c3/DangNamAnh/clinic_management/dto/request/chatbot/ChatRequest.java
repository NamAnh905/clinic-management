package dh12c3.DangNamAnh.clinic_management.dto.request.chatbot;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)

public class ChatRequest {
    String message;
}
