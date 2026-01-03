// statistic.component.ts
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
  imports: [
    CommonModule,
    FormsModule,
    ChartModule,
    CalendarModule,
    TableModule
  ],
  templateUrl: './statistic.component.html',
  styleUrls: ['./statistic.component.scss']
})
export class StatisticComponent implements OnInit {
  stats: DashboardSummary | null = null;
  rangeDates: Date[] | undefined;
  isLoading: boolean = false;

  barData: any;
  barOptions: any;
  pieData: any;
  pieOptions: any;

  private statisticService = inject(StatisticService);

  ngOnInit() {
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

    // 1. Bar Chart
    this.barData = {
      // [FIX] Format nhãn ngày từ "YYYY-MM-DD" -> "DD/MM" cho đẹp
      labels: this.stats.revenueOverTime.map(d => {
        if (d.label && d.label.includes('-')) {
            const parts = d.label.split('-'); // Tách chuỗi 2025-12-26
            return `${parts[2]}/${parts[1]}`; // Trả về 26/12
        }
        return d.label;
      }),
      datasets: [{
        label: 'Doanh thu',
        data: this.stats.revenueOverTime.map(d => d.value),
        backgroundColor: '#3B82F6',
        borderRadius: 4
      }]
    };

    this.barOptions = {
        plugins: {
            legend: { display: false },
            tooltip: {
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
                ticks: {
                    callback: (value: any) => {
                        if (value >= 1000000) return value / 1000000 + ' Tr';
                        if (value >= 1000) return value / 1000 + ' k';
                        return value;
                    }
                }
            },
            x: {
                grid: { display: false }
            }
        },
        maintainAspectRatio: false
    };

    // 2. Pie Chart
    this.pieData = {
      labels: this.stats.revenueStructure.map(d => d.label === 'SERVICE' ? 'Dịch vụ' : 'Thuốc'),
      datasets: [{
        data: this.stats.revenueStructure.map(d => d.value),
        backgroundColor: ['#10B981', '#F59E0B'],
        hoverOffset: 4
      }]
    };
  }
}
