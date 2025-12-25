package dh12c3.DangNamAnh.clinic_management.dto.response.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class ScheduleResponse {
    Long scheduleId;
    Long doctorId;
    String doctorName;
    Long receptionistId;
    String receptionistName;
    Long specialtyId;
    String specialty;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate workDate;

    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime startTime;

    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime endTime;
}
