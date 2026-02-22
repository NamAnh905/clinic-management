import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../../../api/auth.service';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, ToastModule],
  providers: [MessageService],
  templateUrl: './forgot-password.component.html',
  styleUrl: './forgot-password.component.scss'
})
export class ForgotPasswordComponent {
  step = 1; // 1: Email, 2: OTP, 3: Reset
  forgotForm: FormGroup;
  isLoading = false;

  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private messageService = inject(MessageService);
  private router = inject(Router);

  constructor() {
    this.forgotForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      otp: ['', [Validators.required, Validators.pattern('^[0-9]{6}$')]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('newPassword')?.value === g.get('confirmPassword')?.value ? null : { mismatch: true };
  }

  sendOtp() {
    if (this.forgotForm.get('email')?.invalid) return;
    this.isLoading = true;
    this.authService.forgotPassword(this.forgotForm.value.email).subscribe({
      next: () => {
        this.step = 2;
        this.isLoading = false;
        this.messageService.add({ severity: 'info', summary: 'Thông báo', detail: 'Mã OTP đã được gửi tới Email' });
      },
      error: (err) => {
        this.isLoading = false;
        this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: err.error?.message || 'Email không tồn tại' });
      }
    });
  }

  verifyOtp() {
    if (this.forgotForm.get('otp')?.valid) this.step = 3;
  }

  handleResetPassword() {
    if (this.forgotForm.invalid) return;
    this.isLoading = true;
    this.authService.resetPassword(this.forgotForm.value).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Mật khẩu đã được thay đổi' });
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      error: (err) => {
        this.isLoading = false;
        this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: err.error?.message || 'Mã OTP không đúng' });
      }
    });
  }
}
