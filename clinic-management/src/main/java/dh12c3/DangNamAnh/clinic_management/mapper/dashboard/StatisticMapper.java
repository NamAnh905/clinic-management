package dh12c3.DangNamAnh.clinic_management.mapper.dashboard;

import dh12c3.DangNamAnh.clinic_management.dto.response.dashboard.RevenueResponse;
import org.mapstruct.Mapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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

    // Helper format ngày
    default String formatDate(LocalDate date) {
        return date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
    }
}
