import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

// PrimeNG
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

// Services & Models
import { AppointmentService } from '../../../api/appointment.service';
import { BillingService } from '../../../api/billing.service';
import { AppointmentResponse } from '../../../models/appointment.model';

@Component({
  selector: 'app-user-appointment',
  standalone: true,
  imports: [CommonModule, TableModule, ButtonModule, ToastModule],
  providers: [MessageService],
  templateUrl: './user-appointment.component.html',
  styleUrls: ['./user-appointment.component.scss']
})
export class UserAppointmentComponent implements OnInit {

  myAppointments: AppointmentResponse[] = [];
  loadingHistory: boolean = false;

  // THÊM: Các biến phục vụ phân trang
  totalRecords: number = 0;
  page: number = 0;
  size: number = 5;

  private appointmentService = inject(AppointmentService);
  private billingService = inject(BillingService);
  private messageService = inject(MessageService);
  private router = inject(Router);

  ngOnInit() {
    this.loadMyAppointments();
  }

  loadMyAppointments() {
    // 1. Logic Cache (Giống InvoiceComponent)
    // Nếu có cache và đang ở trang đầu tiên thì hiển thị ngay
    if (this.appointmentService.appointmentsCache && this.appointmentService.appointmentsCache.length > 0 && this.page === 0) {
      this.myAppointments = this.appointmentService.appointmentsCache;
      // Lưu ý: Nếu service có lưu totalRecords thì lấy ra, tạm thời giả định cache chỉ lưu data trang đầu
      this.loadingHistory = false;
    } else {
      this.loadingHistory = true;
    }

    // 2. Gọi API với tham số động (page + 1, size)
    this.appointmentService.getAppointments(this.page + 1, this.size).subscribe({
      next: (res) => {
        if (res.result) {
            // Cập nhật data và tổng số bản ghi từ server
            this.myAppointments = (res.result as any).data || [];
            this.totalRecords = res.result.totalElements;

            // Cập nhật lại cache nếu đang ở trang 1 (tuỳ chọn)
            if (this.page === 0) {
                this.appointmentService.appointmentsCache = this.myAppointments;
            }
        }
        this.loadingHistory = false;
      },
      error: (err) => {
        this.loadingHistory = false;
        if (this.myAppointments.length === 0) {
            console.error(err);
            this.messageService.add({severity: 'error', summary: 'Lỗi', detail: 'Không tải được lịch sử.'});
        }
      }
    });
  }

  // THÊM: Hàm xử lý khi người dùng bấm chuyển trang
  onPageChange(event: any) {
    this.page = event.first / event.rows;
    this.size = event.rows;
    this.loadMyAppointments();
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
             // ... (Giữ nguyên logic xử lý lỗi cũ) ...
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
          },
          error: () => {
              this.loadingHistory = false;
              this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Lỗi kết nối VNPay.' });
          }
      });
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
