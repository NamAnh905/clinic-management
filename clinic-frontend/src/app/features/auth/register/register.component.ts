import { Component, inject } from '@angular/core';
import { CommonModule, formatDate } from '@angular/common'; // Import formatDate
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';

// PrimeNG Imports
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';
import { CalendarModule } from 'primeng/calendar'; // <--- Dùng Module
import { DropdownModule } from 'primeng/dropdown';

import { UserService } from '../../../core/services/user.service';
import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    ToastModule,
    CalendarModule,
    DropdownModule
  ],
  providers: [MessageService],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  registerForm: FormGroup;
  isLoading = false;
  showPassword = false;
  showConfirmPassword = false;

  // Option cho Giới tính
  genderOptions = [
    { label: 'Nam', value: 'MALE' },
    { label: 'Nữ', value: 'FEMALE' },
    { label: 'Khác', value: 'OTHER' }
  ];

  private fb = inject(FormBuilder);
  private userService = inject(UserService);
  private authService = inject(AuthService);
  private router = inject(Router);
  private messageService = inject(MessageService);

  constructor() {
    this.registerForm = this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(2)]],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]],

      // --- CÁC TRƯỜNG MỚI ---
      phoneNumber: ['', [Validators.required, Validators.pattern(/(84|0[3|5|7|8|9])+([0-9]{8})\b/)]], // Regex SĐT Việt Nam
      address: [''], // Optional
      gender: [null, [Validators.required]],
      dateOfBirth: [null, [Validators.required]]
    }, { validators: this.passwordMatchValidator });
  }

  passwordMatchValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
    const password = control.get('password')?.value;
    const confirmPassword = control.get('confirmPassword')?.value;
    return password === confirmPassword ? null : { mismatch: true };
  };

  toggleVisibility(field: 'password' | 'confirm') {
    if (field === 'password') this.showPassword = !this.showPassword;
    else this.showConfirmPassword = !this.showConfirmPassword;
  }

  isFieldInvalid(fieldName: string): boolean {
    const field = this.registerForm.get(fieldName);
    return !!(field && field.invalid && (field.dirty || field.touched));
  }

  onSubmit() {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    const formValue = this.registerForm.value;

    // Chuẩn bị dữ liệu đúng Interface RegisterRequest
    const requestData = {
      fullName: formValue.fullName,
      email: formValue.email,
      password: formValue.password,
      phoneNumber: formValue.phoneNumber,
      address: formValue.address,
      gender: formValue.gender, // Enum: MALE, FEMALE
      // Format ngày sinh: YYYY-MM-DD
      dateOfBirth: formValue.dateOfBirth ? formatDate(formValue.dateOfBirth, 'yyyy-MM-dd', 'en-US') : ''
    };

    this.userService.register(requestData as any).subscribe({
      next: (response: any) => {
        this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đăng ký thành công!' });
        setTimeout(() => this.router.navigate(['/login']), 1500);
      },
      error: (err: any) => {
        this.isLoading = false;
        const errorMsg = err.error?.message || 'Đăng ký thất bại. Vui lòng thử lại!';
        this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: errorMsg });
      }
    });
  }
}
