package dh12c3.DangNamAnh.clinic_management.dto.request.schedule;

import dh12c3.DangNamAnh.clinic_management.validation.ValidTimeRange;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
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
@ValidTimeRange

public class ScheduleCreationRequest {

    Long doctorId;
    Long receptionistId;

    @NotNull(message = "Work date cannot be empty.")
    @Future(message = "Working date must be strictly in the future.")
    LocalDate workDate;

    @NotNull(message = "Start time cannot be empty.")
    LocalTime startTime;

    @NotNull(message = "End time cannot be empty.")
    LocalTime endTime;
}
