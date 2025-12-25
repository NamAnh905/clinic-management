package dh12c3.DangNamAnh.clinic_management.service.chatbot;

import dh12c3.DangNamAnh.clinic_management.dto.response.schedule.ScheduleResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.master.SpecialtyResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.staff.DoctorResponse;
import dh12c3.DangNamAnh.clinic_management.helper.ChatPromptUtils;
import dh12c3.DangNamAnh.clinic_management.service.schedule.WorkingScheduleService;
import dh12c3.DangNamAnh.clinic_management.service.staff.DoctorService;
import dh12c3.DangNamAnh.clinic_management.service.master.SpecialtyService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ChatService {

    DoctorService doctorService;
    SpecialtyService specialtyService;
    WorkingScheduleService workingScheduleService;
    RestTemplate restTemplate = new RestTemplate();

    @NonFinal
    @Value("${gemini.api.key}")
    String myKey;

    @NonFinal
    @Value("${gemini.api.url}")
    String geminiUrl;

    public String getAnswer(String userMessage) {
        try {
            String dynamicData = buildClinicContext(null, null, null, null);
            String finalPrompt = String.format(ChatPromptUtils.PROMPT_TEMPLATE,
                    "Phòng khám 28Care",
                    ChatPromptUtils.getClinicInfo(),
                    dynamicData,
                    userMessage
            );

            // 3. Gọi API
            return callGeminiApi(finalPrompt);

        } catch (Exception e) {
            log.error("Lỗi Chatbot: ", e);
            return "Hiện tại mình đang kiểm tra lại hệ thống, bạn đợi chút nhé!";
        }
    }

    String buildClinicContext(Long doctorId, Long receptionistId, Long specialtyId, String viewType) {
        try {
            StringBuilder context = new StringBuilder();

            // 1. Lấy Chuyên khoa
            List<SpecialtyResponse> specialties = specialtyService.findAll("", 1, 50).getData();
            context.append("--- DANH SÁCH CHUYÊN KHOA ---\n");
            if (specialties != null) {
                context.append(specialties.stream().map(SpecialtyResponse::getName).collect(Collectors.joining(", ")));
            }
            context.append("\n\n");

            // 2. Lấy Bác sĩ
            List<DoctorResponse> doctors = doctorService.findAllDoctors(null, "", 1, 50).getData();
            context.append("--- DANH SÁCH BÁC SĨ ---\n");
            if (doctors != null) {
                for (DoctorResponse doc : doctors) {
                    context.append(String.format("- Bác sĩ %s (Khoa %s)\n", doc.getFullName(), doc.getSpecialtyName()));
                }
            }
            context.append("\n");

            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(7);

            List<ScheduleResponse> schedules = workingScheduleService
                    .findAllSchedules(1, 50, doctorId, receptionistId, specialtyId, startDate, endDate, viewType).getData();

            context.append("--- LỊCH LÀM VIỆC SẮP TỚI ---\n");

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            if (schedules != null && !schedules.isEmpty()) {
                for (ScheduleResponse sch : schedules) {
                    // Format: BS Nam Anh: 12/12/2025 (08:00 - 12:00)
                    String dateStr = sch.getWorkDate().format(dateFormatter);
                    String startStr = sch.getStartTime().format(timeFormatter);
                    String endStr = sch.getEndTime().format(timeFormatter);

                    context.append(String.format("- BS %s: Ngày %s (%s - %s)\n",
                            sch.getDoctorName(), dateStr, startStr, endStr));
                }
            } else {
                context.append("(Hiện chưa có lịch trong 7 ngày tới)\n");
            }

            return context.toString();
        } catch (Exception e) {
            log.error("Lỗi lấy data: ", e);
            return "Hiện chưa lấy được dữ liệu chi tiết.";
        }
    }

    private String callGeminiApi(String prompt) {
        Map<String, Object> textPart = new HashMap<>();
        textPart.put("text", prompt);
        Map<String, Object> contentParts = new HashMap<>();
        contentParts.put("parts", List.of(textPart));
        Map<String, Object> body = new HashMap<>();
        body.put("contents", List.of(contentParts));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(geminiUrl)
                    .queryParam("key", myKey)
                    .build().toUri();

            ResponseEntity<Map> response = restTemplate.postForEntity(uri, entity, Map.class);

            if (response.getBody() != null) {
                List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.getBody().get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        return (String) parts.get(0).get("text");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Lỗi Gemini: {}", e.getMessage());
            return "Hệ thống đang bận.";
        }
        return "Xin lỗi, tôi chưa hiểu ý bạn.";
    }
}