package dh12c3.DangNamAnh.clinic_management.dto.response.dashboard;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class DashboardSummaryResponse {
    // 1. Summary Cards
    BigDecimal totalRevenue;
    Long totalInvoices;
    BigDecimal todayRevenue;
    Double growthRate;

    List<ChartDataResponse> revenueOverTime;
    List<ChartDataResponse> revenueStructure;

    List<ChartDataResponse> topServices;
    List<ChartDataResponse> topDoctors;
}