package dh12c3.DangNamAnh.clinic_management.dto.request.schedule;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleUpdationRequest {
    LocalDate workDate;
    LocalTime startTime;
    LocalTime endTime;
}
