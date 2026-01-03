package dh12c3.DangNamAnh.clinic_management.mapper.dashboard;

import dh12c3.DangNamAnh.clinic_management.dto.response.dashboard.ChartDataResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.dashboard.DashboardSummaryResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.dashboard.RevenueResponse;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring")
public interface StatisticMapper {

    default RevenueResponse toRevenueResponse(BigDecimal total, BigDecimal drug, BigDecimal service, LocalDate from, LocalDate to) {
        if (total == null) total = BigDecimal.ZERO;
        if (drug == null) drug = BigDecimal.ZERO;
        if (service == null) service = BigDecimal.ZERO;

        String periodStr = formatDate(from) + " - " + formatDate(to);

        return RevenueResponse.builder()
                .period(periodStr)
                .totalRevenue(total)
                .drugRevenue(drug)
                .serviceRevenue(service)
                .build();
    }

    default DashboardSummaryResponse toDashboardSummary(
            BigDecimal totalRevenue,
            Long totalInvoices,
            BigDecimal todayRevenue,
            BigDecimal yesterdayRevenue,
            List<ChartDataResponse> revenueOverTime,
            List<ChartDataResponse> revenueStructure,
            List<ChartDataResponse> topServices,
            List<ChartDataResponse> topDoctors
    ) {
        totalRevenue = (totalRevenue != null) ? totalRevenue : BigDecimal.ZERO;
        todayRevenue = (todayRevenue != null) ? todayRevenue : BigDecimal.ZERO;
        yesterdayRevenue = (yesterdayRevenue != null) ? yesterdayRevenue : BigDecimal.ZERO;
        totalInvoices = (totalInvoices != null) ? totalInvoices : 0L;

        if (revenueOverTime == null) revenueOverTime = Collections.emptyList();
        if (revenueStructure == null) revenueStructure = Collections.emptyList();
        if (topServices == null) topServices = Collections.emptyList();
        if (topDoctors == null) topDoctors = Collections.emptyList();

        double growth = calculateGrowth(todayRevenue, yesterdayRevenue);

        return DashboardSummaryResponse.builder()
                .totalRevenue(totalRevenue)
                .totalInvoices(totalInvoices)
                .todayRevenue(todayRevenue)
                .growthRate(growth)
                .revenueOverTime(revenueOverTime)
                .revenueStructure(revenueStructure)
                .topServices(topServices)
                .topDoctors(topDoctors)
                .build();
    }

    // --- Helper Methods ---
    default String formatDate(LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
    }

    default double calculateGrowth(BigDecimal today, BigDecimal yesterday) {
        if (yesterday.compareTo(BigDecimal.ZERO) > 0) {
            return today.subtract(yesterday)
                    .divide(yesterday, 2, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        } else if (today.compareTo(BigDecimal.ZERO) > 0) {
            return 100.0;
        }
        return 0.0;
    }
}