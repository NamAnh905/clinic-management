package dh12c3.DangNamAnh.clinic_management.service.dashboard;

import dh12c3.DangNamAnh.clinic_management.dto.response.dashboard.RevenueResponse;
import dh12c3.DangNamAnh.clinic_management.mapper.dashboard.StatisticMapper;
import dh12c3.DangNamAnh.clinic_management.repository.billing.InvoiceDetailRepository;
import dh12c3.DangNamAnh.clinic_management.repository.billing.InvoiceRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)

public class StatisticService {
    InvoiceRepository invoiceRepository;
    InvoiceDetailRepository invoiceDetailRepository;
    StatisticMapper statisticMapper; // Inject Mapper vào

    public RevenueResponse getRevenueReport(LocalDate fromDate, LocalDate toDate) {
        // 1. Xử lý thời gian:
        // Nếu không truyền ngày -> Mặc định lấy tháng hiện tại
        if (fromDate == null) fromDate = LocalDate.now().withDayOfMonth(1);
        if (toDate == null) toDate = LocalDate.now();

        // Chuyển LocalDate (chỉ ngày) sang LocalDateTime (ngày + giờ) để query DB chuẩn
        // Từ 00:00:00 ngày bắt đầu -> 23:59:59 ngày kết thúc
        LocalDateTime startDateTime = fromDate.atStartOfDay();
        LocalDateTime endDateTime = toDate.atTime(LocalTime.MAX);

        // 2. Gọi Repository lấy số liệu
        BigDecimal total = invoiceRepository.sumTotalRevenueBetween(startDateTime, endDateTime);
        BigDecimal drugRev = invoiceDetailRepository.sumDrugRevenueBetween(startDateTime, endDateTime);
        BigDecimal serviceRev = invoiceDetailRepository.sumServiceRevenueBetween(startDateTime, endDateTime);

        // 3. Gọi Mapper để đóng gói (Thay vì builder loằng ngoằng ở đây)
        return statisticMapper.toRevenueResponse(total, drugRev, serviceRev, fromDate, toDate);
    }
}
