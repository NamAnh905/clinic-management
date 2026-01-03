package dh12c3.DangNamAnh.clinic_management.controller.dashboard;

import dh12c3.DangNamAnh.clinic_management.dto.response.ApiResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.dashboard.DashboardSummaryResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.dashboard.RevenueResponse;
import dh12c3.DangNamAnh.clinic_management.service.dashboard.StatisticService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/revenue")

public class StatisticController {
    StatisticService statisticService;

    @GetMapping("/dashboard") // Đổi endpoint hoặc thêm mới
    public ApiResponse<DashboardSummaryResponse> getDashboardStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ApiResponse.<DashboardSummaryResponse>builder()
                .result(statisticService.getDashboardStats(startDate, endDate))
                .build();
    }
}
