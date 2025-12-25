import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

// PrimeNG
import { ToastModule } from 'primeng/toast';
import { MessageService } from 'primeng/api';

// Services
import { AuthService } from '../../../core/services/auth.service';
import { UserService } from '../../../core/services/user.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, ToastModule],
  providers: [MessageService],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  loginForm: FormGroup;
  isLoading = false;
  showPassword = false;

  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private userService = inject(UserService);
  private router = inject(Router);
  private messageService = inject(MessageService);

  constructor() {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      remember: [false]
    });
  }

  // 1. Thêm hàm kiểm tra lỗi (Để HTML dùng *ngIf="isFieldInvalid(...)")
  isFieldInvalid(fieldName: string): boolean {
    const field = this.loginForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  // 2. Đổi tên hàm cho khớp với HTML (togglePassword -> togglePasswordVisibility)
  togglePasswordVisibility() {
    this.showPassword = !this.showPassword;
  }

  onSubmit() {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const { email, password } = this.loginForm.value;

    // Gọi API Login
    this.authService.login({ email, password }).subscribe({
      next: (res) => {
        if (res.result?.token) {
          localStorage.setItem('token', res.result.token);
          // Gọi tiếp API lấy Role để điều hướng
          this.checkRoleAndRedirect();
        } else {
            this.isLoading = false;
            this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không nhận được token xác thực.' });
        }
      },
      error: (err) => {
        this.isLoading = false;
        // Xử lý thông báo lỗi an toàn hơn
        const msg = err.error?.message || 'Email hoặc mật khẩu không đúng!';
        this.messageService.add({ severity: 'error', summary: 'Đăng nhập thất bại', detail: msg });
      }
    });
  }

  checkRoleAndRedirect() {
    this.userService.getMyInfo().subscribe({
      next: (res) => {
        const user = res.result;

        if (!user) {
           this.isLoading = false;
           this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không lấy được thông tin tài khoản.' });
           return;
        }

        const roles = user.roles ? user.roles.map((r: any) => r.name) : [];

        this.messageService.add({ severity: 'success', summary: 'Chào mừng', detail: `Xin chào ${user.fullName}!` });

        setTimeout(() => {
          if (roles.includes('ADMIN') || roles.includes('DOCTOR') || roles.includes('RECEPTIONIST')) {
            this.router.navigate(['/admin']);
          } else {
            this.router.navigate(['/home']);
          }
        }, 1000);
      },
      error: (err) => {
        this.isLoading = false;
        console.error(err); // In lỗi ra để debug
        this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể kết nối đến server.' });
      }
    });
  }
}
