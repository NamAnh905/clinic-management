import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

// PrimeNG
import { DialogModule } from 'primeng/dialog';
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { TagModule } from 'primeng/tag';
import { MessageService } from 'primeng/api';

// Services & Models
import { AppointmentService } from '../../../core/services/appointment.service';
import { BillingService } from '../../../core/services/billing.service';
import { AppointmentResponse } from '../../../models/appointment.model';

@Component({
  selector: 'app-appointment-history-dialog',
  standalone: true,
  imports: [CommonModule, DialogModule, TableModule, ButtonModule, TagModule],
  templateUrl: './appointment-history-dialog.component.html',
  styleUrls: ['./appointment-history-dialog.component.scss']
})
export class AppointmentHistoryDialogComponent {
  @Input() visible: boolean = false;
  @Output() visibleChange = new EventEmitter<boolean>();

  myAppointments: AppointmentResponse[] = [];
  loadingHistory: boolean = false;

  private appointmentService = inject(AppointmentService);
  private billingService = inject(BillingService);
  private messageService = inject(MessageService);
  private router = inject(Router);

  // Khi người dùng mở dialog (visible = true), ta load dữ liệu
  ngOnChanges() {
    if (this.visible) {
      this.loadMyAppointments();
    }
  }

  close() {
    this.visible = false;
    this.visibleChange.emit(false);
  }

  loadMyAppointments() {
    this.loadingHistory = true;
    this.appointmentService.getAppointments(1, 100).subscribe({
      next: (res) => {
        this.myAppointments = res.result?.data || [];
        this.loadingHistory = false;
      },
      error: (err) => {
        this.loadingHistory = false;
        console.error(err);
        this.messageService.add({severity: 'error', summary: 'Lỗi', detail: 'Không tải được lịch sử.'});
      }
    });
  }

  payBooking(appointmentId: number) {
    localStorage.setItem('paymentReturnUrl', this.router.url);
    this.loadingHistory = true;
    this.messageService.add({ severity: 'info', summary: 'Đang xử lý', detail: 'Đang kết nối cổng thanh toán...' });

    this.billingService.createBookingInvoice(appointmentId).subscribe({
        next: (res) => {
            if (res.result) this.initiateVnPay(res.result.invoiceId);
        },
        error: (err) => {
            if (err.error?.code === 1008 || err.status === 400) {
                 this.billingService.getInvoiceByAppointment(appointmentId).subscribe({
                     next: (invRes) => {
                         if (invRes.result && invRes.result.paymentStatus === 'PENDING') {
                             this.initiateVnPay(invRes.result.invoiceId);
                         } else {
                             this.loadingHistory = false;
                             this.messageService.add({ severity: 'warn', summary: 'Thông báo', detail: 'Hóa đơn không hợp lệ.' });
                         }
                     },
                     error: () => {
                         this.loadingHistory = false;
                         this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không tìm thấy hóa đơn.' });
                     }
                 });
            } else {
                this.loadingHistory = false;
                this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: err.error?.message || 'Lỗi tạo thanh toán.' });
            }
        }
    });
  }

  private initiateVnPay(invoiceId: number) {
      this.billingService.initiateVnPayPayment(invoiceId).subscribe({
          next: (res) => {
              if (res.result) window.location.href = res.result;
              else {
                  this.loadingHistory = false;
                  this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không lấy được link thanh toán.' });
              }
          },
          error: () => {
              this.loadingHistory = false;
              this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Lỗi kết nối VNPay.' });
          }
      });
  }

  getSeverity(status: string) {
    switch (status) {
        case 'CONFIRMED': return 'success';
        case 'PENDING': return 'warning';
        case 'COMPLETED': return 'info';
        case 'CANCELLED': return 'danger';
        default: return 'secondary';
    }
  }

  getStatusLabel(status: string): string {
    switch (status) {
        case 'PENDING': return 'Chờ thanh toán';
        case 'CONFIRMED': return 'Đã xác nhận';
        case 'COMPLETED': return 'Đã khám';
        case 'CANCELLED': return 'Đã hủy';
        case 'NO_SHOW': return 'Vắng mặt';
        default: return status;
    }
  }
}
