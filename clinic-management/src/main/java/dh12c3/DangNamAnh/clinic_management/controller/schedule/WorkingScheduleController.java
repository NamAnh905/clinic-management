package dh12c3.DangNamAnh.clinic_management.controller.schedule;

import dh12c3.DangNamAnh.clinic_management.dto.request.schedule.ScheduleCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.schedule.ScheduleUpdationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.ApiResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.schedule.ScheduleResponse;
import dh12c3.DangNamAnh.clinic_management.service.schedule.WorkingScheduleService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("schedules")

public class WorkingScheduleController {
    WorkingScheduleService workingScheduleService;

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @PostMapping
    public ApiResponse<ScheduleResponse> create(@RequestBody @Valid ScheduleCreationRequest request){
        return ApiResponse.<ScheduleResponse>builder()
                .result(workingScheduleService.createSchedule(request))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('READ_SCHEDULE')")
    @GetMapping
    public ApiResponse<PageResponse<ScheduleResponse>> findAll(@RequestParam(defaultValue = "1") int page,
                                                               @RequestParam(defaultValue = "10") int size,
                                                               @RequestParam(required = false) Long doctorId,
                                                               @RequestParam(required = false) Long receptionistId,
                                                               @RequestParam(required = false) Long specialtyId,
                                                               @RequestParam(required = false) LocalDate startDate,
                                                               @RequestParam(required = false) LocalDate endDate,
                                                               @RequestParam(required = false) String viewType
    ) {
        return ApiResponse.<PageResponse<ScheduleResponse>>builder()
                .result(workingScheduleService.findAllSchedules(page, size, doctorId, receptionistId, specialtyId, startDate, endDate, viewType))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @PutMapping("/{scheduleId}")
    public ApiResponse<ScheduleResponse> update(@RequestBody ScheduleUpdationRequest request, @PathVariable Long scheduleId){
        return ApiResponse.<ScheduleResponse>builder()
                .result(workingScheduleService.update(request, scheduleId))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @GetMapping("/{scheduleId}")
    public ApiResponse<ScheduleResponse> findById(@PathVariable Long scheduleId){
        return ApiResponse.<ScheduleResponse>builder()
                .result(workingScheduleService.findScheduleById(scheduleId))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @DeleteMapping("/{scheduleId}")
    public void delete(@PathVariable Long scheduleId){
        workingScheduleService.deleteScheduleById(scheduleId);
    }
}
