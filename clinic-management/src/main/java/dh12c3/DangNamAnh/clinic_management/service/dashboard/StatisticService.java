package dh12c3.DangNamAnh.clinic_management.service.dashboard;

import dh12c3.DangNamAnh.clinic_management.dto.response.dashboard.ChartDataResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.dashboard.DashboardSummaryResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.dashboard.RevenueResponse;
import dh12c3.DangNamAnh.clinic_management.mapper.dashboard.StatisticMapper;
import dh12c3.DangNamAnh.clinic_management.repository.billing.InvoiceDetailRepository;
import dh12c3.DangNamAnh.clinic_management.repository.billing.InvoiceRepository;
import dh12c3.DangNamAnh.clinic_management.service.ExcelExportService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)
public class StatisticService {
    InvoiceRepository invoiceRepository;
    InvoiceDetailRepository invoiceDetailRepository;
    StatisticMapper statisticMapper;
    ExcelExportService excelExportService;

    public DashboardSummaryResponse getDashboardStats(LocalDate fromDate, LocalDate toDate) {
        // 1. Setup thời gian
        if (fromDate == null) fromDate = LocalDate.now().withDayOfMonth(1);
        if (toDate == null) toDate = LocalDate.now();

        LocalDateTime start = fromDate.atStartOfDay();
        LocalDateTime end = toDate.atTime(LocalTime.MAX);

        // 2. Setup hôm nay vs hôm qua (để tính tăng trưởng)
        LocalDateTime startToday = LocalDate.now().atStartOfDay();
        LocalDateTime endToday = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime startYesterday = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime endYesterday = LocalDate.now().minusDays(1).atTime(LocalTime.MAX);

        // --- FETCH DỮ LIỆU ---

        // A. Chỉ số tổng quan
        BigDecimal totalRev = invoiceRepository.sumTotalRevenueBetween(start, end);
        Long countInv = invoiceRepository.countPaidInvoicesBetween(start, end);
        BigDecimal revToday = invoiceRepository.sumTotalRevenueBetween(startToday, endToday);
        BigDecimal revYesterday = invoiceRepository.sumTotalRevenueBetween(startYesterday, endYesterday);

        // B. Biểu đồ cột (Doanh thu theo ngày)
        // [QUAN TRỌNG]: Mapping thủ công từ Object[] (Native Query) sang DTO
        List<Object[]> rawDateData = invoiceRepository.getRevenueOverTimeNative(start, end);
        List<ChartDataResponse> revChart = new ArrayList<>();

        for (Object[] row : rawDateData) {
            // row[0] là ngày (Date/String), row[1] là tổng tiền (BigDecimal)
            String label = row[0].toString();
            BigDecimal value = (BigDecimal) row[1];
            revChart.add(new ChartDataResponse(label, value));
        }

        // C. Biểu đồ tròn & Top list (Đã dùng JPQL select new nên có sẵn DTO)
        List<ChartDataResponse> structChart = invoiceDetailRepository.getRevenueStructure(start, end);

        List<ChartDataResponse> topItems = invoiceDetailRepository.getTopSoldItems(start, end);
        if(topItems.size() > 5) topItems = topItems.subList(0, 5);

        List<ChartDataResponse> topDocs = invoiceRepository.getTopDoctors(start, end);
        if(topDocs.size() > 5) topDocs = topDocs.subList(0, 5);

        // 3. Đóng gói trả về
        return statisticMapper.toDashboardSummary(
                totalRev,
                countInv,
                revToday,
                revYesterday,
                revChart,
                structChart,
                topItems,
                topDocs
        );
    }

    public ByteArrayInputStream exportRevenueExcel(LocalDate fromDate, LocalDate toDate) throws IOException {
        // 1. Setup thời gian mặc định nếu không truyền vào
        if (fromDate == null) fromDate = LocalDate.now().withDayOfMonth(1);
        if (toDate == null) toDate = LocalDate.now();

        LocalDateTime start = fromDate.atStartOfDay();
        LocalDateTime end = toDate.atTime(LocalTime.MAX);

        // 2. Lấy dữ liệu chi tiết từ Repository (Gọi hàm mới tạo ở Bước 1)
        List<Object[]> rawData = invoiceRepository.getDetailedRevenueExportNative(start, end);
        List<RevenueResponse> exportData = new ArrayList<>();

        for (Object[] row : rawData) {
            String period = row[0].toString();

            // Ép kiểu an toàn từ Object sang BigDecimal
            BigDecimal totalRev = row[1] != null ? new BigDecimal(row[1].toString()) : BigDecimal.ZERO;
            BigDecimal drugRev = row[2] != null ? new BigDecimal(row[2].toString()) : BigDecimal.ZERO;
            BigDecimal serviceRev = row[3] != null ? new BigDecimal(row[3].toString()) : BigDecimal.ZERO;

            exportData.add(RevenueResponse.builder()
                    .period(period)
                    .totalRevenue(totalRev)
                    .drugRevenue(drugRev)
                    .serviceRevenue(serviceRev)
                    .build());
        }

        // 3. Gọi hàm xuất file Excel dùng chung
        return excelExportService.exportToExcel(exportData, "Báo cáo doanh thu chi tiết");
    }
}