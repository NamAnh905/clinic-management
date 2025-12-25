import { Component, inject, OnInit } from '@angular/core';
import { CommonModule, formatDate } from '@angular/common';
import { Router } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

// PrimeNG Modules
import { AvatarModule } from 'primeng/avatar';
import { MenuModule } from 'primeng/menu';
import { BreadcrumbModule } from 'primeng/breadcrumb';
import { MenuItem, MessageService } from 'primeng/api';
import { DialogModule } from 'primeng/dialog'; // Thêm Dialog
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { CalendarModule } from 'primeng/calendar';
import { DropdownModule } from 'primeng/dropdown';
import { ToastModule } from 'primeng/toast';

// Services & Models
import { AuthService } from '../../../../core/services/auth.service';
import { BreadcrumbService } from '../../../../core/services/breadcrumb.service';
import { UserService } from '../../../../core/services/user.service';
import { UserResponse } from '../../../../models/user.model';

@Component({
  selector: 'app-admin-header',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    AvatarModule, MenuModule, BreadcrumbModule,
    DialogModule, ButtonModule, InputTextModule,
    CalendarModule, DropdownModule, ToastModule
  ],
  providers: [MessageService], // Cung cấp MessageService để hiện thông báo
  templateUrl: './admin-header.component.html',
  styleUrls: ['./admin-header.component.scss']
})
export class AdminHeaderComponent implements OnInit {
  private authService = inject(AuthService);
  private userService = inject(UserService);
  private router = inject(Router);
  private breadcrumbService = inject(BreadcrumbService);
  private messageService = inject(MessageService);
  private fb = inject(FormBuilder);

  userMenuItems: MenuItem[] = [];
  breadcrumbItems: MenuItem[] = [];
  home: MenuItem = { icon: 'pi pi-home', routerLink: '/admin' };

  // Thông tin User hiện tại
  currentUser: UserResponse | null = null;
  userRoleName: string = 'User';

  passwordDialog: boolean = false;
  passwordForm: FormGroup;

  // Profile Dialog & Form
  profileDialog: boolean = false;
  profileForm: FormGroup;
  genderOptions = [{ label: 'Nam', value: 'MALE' }, { label: 'Nữ', value: 'FEMALE' }];

  constructor() {
    this.profileForm = this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(5)]],
      email: [{ value: '', disabled: true }], // Email không được sửa
      phoneNumber: ['', [Validators.pattern(/(84|0[3|5|7|8|9])+([0-9]{8})\b/)]],
      dateOfBirth: [null],
      gender: [null],
      address: ['']
    });

    this.passwordForm = this.fb.group({
      currentPassword: ['', Validators.required], // Mật khẩu hiện tại
      newPassword: ['', [Validators.required, Validators.minLength(6)]], // Mật khẩu mới
      confirmPassword: ['', Validators.required] // Xác nhận mật khẩu
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit() {
    // 1. Lấy thông tin User ngay khi load Header
    this.loadCurrentUser();

    // 2. Breadcrumb logic (Giữ nguyên)
    this.breadcrumbService.breadcrumbs$.subscribe(items => {
      this.breadcrumbItems = items;
      if (this.breadcrumbItems.length > 0) {
        this.breadcrumbItems.forEach(item => item.styleClass = '');
        const lastItem = this.breadcrumbItems[this.breadcrumbItems.length - 1];
        lastItem.styleClass = 'font-bold text-primary';
      }
    });

    // 3. Cấu hình User Menu
    this.userMenuItems = [
        {
          label: 'Hồ sơ cá nhân',
          icon: 'pi pi-user',
          command: () => this.openProfileDialog() // Mở Dialog khi click
        },
        { separator: true },
        {
          label: 'Đổi mật khẩu', // Tính năng mở rộng sau này
          icon: 'pi pi-key',
          command: () => this.openPasswordDialog()
        },
        {
          label: 'Đăng xuất',
          icon: 'pi pi-sign-out',
          styleClass: 'text-red-500',
          command: () => this.logout()
        }
    ];
  }

  openPasswordDialog() {
    this.passwordForm.reset();
    this.passwordDialog = true;
  }

  loadCurrentUser() {
    this.userService.getMyInfo().subscribe({
      next: (res) => {
        if (res.result) {
          this.currentUser = res.result;
          // Map Role Name sang tiếng Việt hiển thị cho đẹp
          this.userRoleName = this.getVietnameseRole(this.currentUser.roles?.[0]?.name);
        }
      },
      error: () => {
        // Nếu lỗi token (hết hạn), có thể đá ra login
        // this.logout();
      }
    });
  }

  openProfileDialog() {
    if (!this.currentUser) return;

    // Fill dữ liệu vào Form
    this.profileForm.patchValue({
      fullName: this.currentUser.fullName,
      email: this.currentUser.email,
      phoneNumber: this.currentUser.phoneNumber,
      address: this.currentUser.address,
      gender: this.currentUser.gender,
      dateOfBirth: this.currentUser.dateOfBirth ? new Date(this.currentUser.dateOfBirth) : null
    });

    this.profileDialog = true;
  }

  saveProfile() {
    if (this.profileForm.invalid) return;

    const formValue = this.profileForm.getRawValue();
    const formattedDob = formValue.dateOfBirth ? formatDate(formValue.dateOfBirth, 'yyyy-MM-dd', 'en-US') : null;

    // Chuẩn bị payload cập nhật
    const updateData = {
      fullName: formValue.fullName,
      phoneNumber: formValue.phoneNumber,
      gender: formValue.gender,
      dateOfBirth: formattedDob,
      address: formValue.address
      // Không gửi password, roles, isActive, email tại đây
    };

    this.userService.updateMyInfo(updateData as any).subscribe({
      next: (res) => {
        this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Cập nhật hồ sơ thành công!' });
        this.profileDialog = false;

        // Cập nhật lại giao diện Header ngay lập tức
        if (res.result) {
          this.currentUser = res.result;
        }
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: err.error?.message || 'Cập nhật thất bại' });
      }
    });
  }

  getVietnameseRole(role: string | undefined): string {
    switch (role) {
      case 'ADMIN': return 'Quản trị viên';
      case 'DOCTOR': return 'Bác sĩ';
      case 'RECEPTIONIST': return 'Lễ tân';
      case 'PATIENT': return 'Bệnh nhân';
      default: return 'Người dùng';
    }
  }

  logout() {
    this.authService.logout().subscribe({
      next: () => {
        localStorage.removeItem('token');
        this.router.navigate(['/login']);
      },
      error: () => {
        localStorage.removeItem('token');
        this.router.navigate(['/login']);
      }
    });
  }

  savePassword() {
    if (this.passwordForm.invalid) return;

    const { currentPassword, newPassword } = this.passwordForm.value;

    // Gọi API updateMyInfo hoặc một API change-password riêng tùy backend
    // Ở đây mình giả định dùng updateMyInfo và backend sẽ tự xử lý nếu có field password
    // HOẶC tốt nhất là bạn nên viết thêm hàm changePassword(current, new) trong UserService

    // Ví dụ gửi request (Logic này phụ thuộc vào Backend của bạn yêu cầu body thế nào)
    const request = {
      oldPassword: currentPassword, // Backend cần check pass cũ
      password: newPassword         // Pass mới
    };

    // Gọi service (Lưu ý: Bạn cần đảm bảo UserService có hàm hỗ trợ hoặc dùng updateMyInfo nếu backend hỗ trợ)
    this.userService.updateMyInfo(request as any).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đổi mật khẩu thành công!' });
        this.passwordDialog = false;
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: err.error?.message || 'Đổi mật khẩu thất bại' });
      }
    });
  }

  passwordMatchValidator: ValidatorFn = (control: AbstractControl): ValidationErrors | null => {
    const newPass = control.get('newPassword');
    const confirmPass = control.get('confirmPassword');
    return newPass && confirmPass && newPass.value !== confirmPass.value ? { mismatch: true } : null;
  };
}
