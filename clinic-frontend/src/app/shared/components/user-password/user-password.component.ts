import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AbstractControl, FormBuilder, FormGroup, ReactiveFormsModule, ValidationErrors, ValidatorFn, Validators } from '@angular/forms';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { MessageService } from 'primeng/api';
import { UserService } from '../../../api/user.service';

@Component({
  selector: 'app-user-password',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, ButtonModule, InputTextModule],
  providers: [MessageService],
  templateUrl: './user-password.component.html',
  styleUrls: ['./user-password.component.scss']
})
export class UserPasswordComponent {
  passwordForm: FormGroup;
  private fb = inject(FormBuilder);
  private userService = inject(UserService);
  private messageService = inject(MessageService);

  constructor() {
    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  savePassword() {
    if (this.passwordForm.invalid) return;
    const { currentPassword, newPassword } = this.passwordForm.value;

    this.userService.updateMyInfo({ oldPassword: currentPassword, password: newPassword } as any).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đổi mật khẩu thành công!' });
        this.passwordForm.reset();
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: err.error?.message || 'Mật khẩu cũ không đúng.' });
      }
    });
  }

  passwordMatchValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
    const newPass = control.get('newPassword');
    const confirmPass = control.get('confirmPassword');
    return newPass && confirmPass && newPass.value !== confirmPass.value ? { mismatch: true } : null;
  };
}
