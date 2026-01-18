import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { ChartModule } from 'primeng/chart';
import { CalendarModule } from 'primeng/calendar';
import { TableModule } from 'primeng/table';
import { StatisticService } from '../../../core/services/statistic.service';
import { DashboardSummary } from '../../../models/dashboard.model';

@Component({
  selector: 'app-statistic',
  standalone: true,
  imports: [CommonModule, FormsModule, ChartModule, CalendarModule, TableModule],
  templateUrl: './statistic.component.html',
  styleUrls: ['./statistic.component.scss']
})
export class StatisticComponent implements OnInit {
  stats: DashboardSummary | null = null;
  rangeDates: Date[] | undefined;
  isLoading: boolean = false;

  // Biến dữ liệu cho biểu đồ
  revenueChartData: any;
  revenueChartOptions: any;
  pieData: any;
  pieOptions: any;

  private statisticService = inject(StatisticService);

  ngOnInit() {
    // Mặc định load dữ liệu từ đầu tháng đến hiện tại
    const today = new Date();
    const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
    this.rangeDates = [firstDay, today];
    this.loadData(firstDay, today);
  }

  onDateChange() {
    if (this.rangeDates && this.rangeDates[1]) {
      this.loadData(this.rangeDates[0], this.rangeDates[1]);
    }
  }

  loadData(start: Date, end: Date) {
    this.isLoading = true;
    this.statisticService.getRevenueStats(start, end).subscribe({
      next: (res) => {
        this.stats = res.result;
        this.initCharts();
        this.isLoading = false;
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
      }
    });
  }

  initCharts() {
    if (!this.stats) return;

    // --- 1. Main Chart: Area Chart (Biểu đồ vùng) ---
    this.revenueChartData = {
      labels: this.stats.revenueOverTime.map(d => {
        // Xử lý format ngày: 2026-01-14 -> 14/01
        if (d.label && d.label.includes('-')) {
            const parts = d.label.split('-');
            // Nếu format là YYYY-MM-DD
            if (parts.length === 3) return `${parts[2]}/${parts[1]}`;
        }
        return d.label;
      }),
      datasets: [{
        label: 'Doanh thu',
        data: this.stats.revenueOverTime.map(d => d.value),
        fill: true,
        borderColor: '#3B82F6', // Blue-500
        backgroundColor: 'rgba(59, 130, 246, 0.1)',
        tension: 0.4,
        pointBackgroundColor: '#ffffff',
        pointBorderColor: '#3B82F6',
        pointBorderWidth: 2,
        pointRadius: 4,
        pointHoverRadius: 6
      }]
    };

    this.revenueChartOptions = {
        plugins: {
            legend: { display: false },
            tooltip: {
                backgroundColor: '#1e293b',
                padding: 12,
                titleFont: { size: 13 },
                bodyFont: { size: 13 },
                displayColors: false,
                callbacks: {
                    label: (context: any) => {
                        let value = context.raw;
                        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
                    }
                }
            }
        },
        scales: {
            y: {
                beginAtZero: true,
                border: { display: false },
                grid: { color: '#f3f4f6', drawBorder: false },
                ticks: {
                    color: '#9ca3af',
                    callback: (value: any) => {
                        if (value >= 1000000) return value / 1000000 + ' Tr';
                        return value / 1000 + ' k';
                    }
                }
            },
            x: {
                border: { display: false },
                grid: { display: false },
                ticks: { color: '#9ca3af' }
            }
        },
        maintainAspectRatio: false,
        interaction: { mode: 'index', intersect: false },
    };

    // --- 2. Pie Chart: Doughnut Chart (Biểu đồ tròn) ---
    // SỬA LẠI LOGIC LẤY MÀU: Dựa vào từ khóa thay vì key cứng

    const getColor = (label: string): string => {
        if (!label) return '#9CA3AF';
        const text = label.toLowerCase().trim();

        // Kiểm tra từ khóa (bất chấp hoa thường)
        if (text.includes('dịch vụ') || text.includes('service')) return '#10B981'; // Xanh lá
        if (text.includes('thuốc') || text.includes('drug')) return '#F59E0B';    // Vàng cam
        if (text.includes('đặt') || text.includes('booking')) return '#3B82F6';    // Xanh dương

        return '#9CA3AF'; // Mặc định xám
    };

    this.pieData = {
      labels: this.stats.revenueStructure.map(d => d.label),
      datasets: [{
        data: this.stats.revenueStructure.map(d => d.value),
        backgroundColor: this.stats.revenueStructure.map(d => getColor(d.label)), // Gọi hàm lấy màu
        borderWidth: 0,
        hoverOffset: 10
      }]
    };

    this.pieOptions = {
        cutout: '75%',
        plugins: {
            legend: {
                position: 'bottom',
                labels: {
                    usePointStyle: true,
                    padding: 20,
                    font: { family: 'Inter', size: 12 },
                    color: '#4b5563'
                }
            },
            tooltip: {
                callbacks: {
                    label: (context: any) => {
                        let value = context.raw;
                        return ' ' + new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(value);
                    }
                }
            }
        }
    };
  }
}
