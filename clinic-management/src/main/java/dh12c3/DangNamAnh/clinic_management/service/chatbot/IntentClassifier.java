package dh12c3.DangNamAnh.clinic_management.service.chatbot;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dh12c3.DangNamAnh.clinic_management.enums.SearchType;

@AiService
public interface IntentClassifier {

    @SystemMessage("""
        Bạn là một bộ phân loại ý định người dùng cho hệ thống phòng khám.
        Nhiệm vụ của bạn là đọc câu hỏi và xác định người dùng đang muốn tìm kiếm loại thông tin nào.
        
        Quy tắc phân loại:
        - Nếu hỏi về người, bác sĩ, ai khám, trình độ... -> Chọn DOCTOR
        - Nếu hỏi về khoa, phòng ban... -> Chọn SPECIALTY
        - Nếu hỏi về giá tiền, chi phí, gói khám, dịch vụ... -> Chọn SERVICE
        - Nếu chào hỏi hoặc không rõ ràng -> Chọn GENERAL
        
        Chỉ trả về đúng một trong các giá trị Enum.
    """)
    SearchType classify(@UserMessage String userQuery);
}
