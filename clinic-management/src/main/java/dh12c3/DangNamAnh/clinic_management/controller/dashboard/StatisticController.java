package dh12c3.DangNamAnh.clinic_management.controller.dashboard;

import dh12c3.DangNamAnh.clinic_management.dto.response.ApiResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.dashboard.DashboardSummaryResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.dashboard.RevenueResponse;
import dh12c3.DangNamAnh.clinic_management.service.dashboard.StatisticService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.IOException;
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

    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('READ_DASHBOARD')") // Thay đổi quyền cho phù hợp với dự án của bạn
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) throws IOException {

        ByteArrayInputStream in = statisticService.exportRevenueExcel(startDate, endDate);

        HttpHeaders headers = new HttpHeaders();
        // File name có thể linh hoạt theo ngày xuất
        headers.add("Content-Disposition", "attachment; filename=revenue_report_" + LocalDate.now() + ".xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}
