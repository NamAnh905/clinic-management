import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { BillingService } from '../../../core/services/billing.service'; // Chỉnh lại đường dẫn nếu cần
import { MessageService } from 'primeng/api';

// PrimeNG Modules
import { ToastModule } from 'primeng/toast';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ButtonModule } from 'primeng/button';

import { ApiResponse } from '../../../models/core.model';
import { InvoiceResponse } from '../../../models/billing.model';
import { UserService } from '../../../core/services/user.service';

@Component({
  selector: 'app-payment-return',
  standalone: true,
  imports: [
    CommonModule,
    ToastModule,
    ProgressSpinnerModule,
    ButtonModule
  ],
  providers: [MessageService],
  templateUrl: './payment-return.component.html',
  styleUrl: './payment-return.component.scss'
})
export class PaymentReturnComponent implements OnInit {
  loading: boolean = true;
  success: boolean = false;
  errorMessage: string = '';

  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private billingService = inject(BillingService);
  private messageService = inject(MessageService);
  private userService = inject(UserService);

  ngOnInit() {
    // Lấy query params từ URL (do VNPay trả về)
    this.route.queryParams.subscribe(params => {
      if (params && Object.keys(params).length > 0) {
        // Có tham số -> Gọi Service xử lý
        this.processPayment(params);
      } else {
        // Không có tham số -> Lỗi
        this.loading = false;
        this.errorMessage = 'Không tìm thấy thông tin giao dịch.';
      }
    });
  }

  processPayment(params: any) {
    this.billingService.processVnPayReturn(params).subscribe({
      // 1. Khai báo kiểu rõ ràng cho 'res' để hết lỗi "implicitly any"
      next: (res: ApiResponse<InvoiceResponse>) => {

        // 2. Sử dụng biến 'res' (in ra log) để hết lỗi "never read"
        console.log('Kết quả thanh toán:', res);

        this.loading = false;
        this.success = true;
        this.messageService.add({
          severity: 'success',
          summary: 'Thành công',
          detail: 'Giao dịch hoàn tất. Hóa đơn đã được cập nhật.'
        });

        setTimeout(() => this.goBack(), 3000);
      },
      // 3. Khai báo kiểu 'any' cho 'err'
      error: (err: any) => {
        console.error('Lỗi thanh toán:', err); // Log lỗi ra để dễ debug

        this.loading = false;
        this.success = false;

        // Lấy message lỗi từ Backend trả về (nếu có)
        this.errorMessage = err.error?.message || 'Giao dịch bị lỗi hoặc đã bị hủy.';

        this.messageService.add({
          severity: 'error',
          summary: 'Thất bại',
          detail: this.errorMessage
        });
      }
    });
  }

  goBack() {
    // 1. Lấy URL cũ đã lưu
    const returnUrl = localStorage.getItem('paymentReturnUrl');

    if (returnUrl) {
      // 2. Xóa đi để không ảnh hưởng lần sau
      localStorage.removeItem('paymentReturnUrl');
      // 3. Quay về đúng nơi đã bắt đầu
      this.router.navigateByUrl(returnUrl);
    } else {
      // 4. FALLBACK: Nếu không có URL lưu (ví dụ user mở tab mới), tự động check role để về trang chủ hoặc admin
      this.checkRoleAndRedirect();
    }
  }

  // Hàm phụ trợ để điều hướng nếu mất dấu vết lịch sử
  private checkRoleAndRedirect() {
      this.userService.getMyInfo().subscribe({
          next: (res) => {
              const roles = res.result?.roles?.map((r: any) => r.name) || [];
              if (roles.includes('ADMIN') || roles.includes('RECEPTIONIST')) {
                  this.router.navigate(['/admin/invoices']);
              } else {
                  this.router.navigate(['/']); // Người dùng thường về trang chủ
              }
          },
          error: () => this.router.navigate(['/'])
      });
  }
}
