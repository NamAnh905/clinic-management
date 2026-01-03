import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, formatDate } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

// PrimeNG Modules
import { AvatarModule } from 'primeng/avatar';
import { MenuModule } from 'primeng/menu';
import { MenuItem, MessageService } from 'primeng/api';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { CalendarModule } from 'primeng/calendar';
import { DropdownModule } from 'primeng/dropdown';
import { ToastModule } from 'primeng/toast';
import { TableModule } from 'primeng/table'; // <--- MỚI: Import Table
import { TagModule } from 'primeng/tag';

// Services
import { UserService } from '../../../../core/services/user.service';
import { AuthService } from '../../../../core/services/auth.service';
import { AppointmentService } from '../../../../core/services/appointment.service';
import { UploadService } from '../../../../core/services/upload.service';
import { UserResponse } from '../../../../models/user.model';
import { AppointmentResponse } from '../../../../models/appointment.model';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule, RouterLink, RouterLinkActive, ReactiveFormsModule,
    AvatarModule, MenuModule, DialogModule, ButtonModule,
    InputTextModule, CalendarModule, DropdownModule, ToastModule,
    TableModule, TagModule
  ],
  providers: [MessageService], // Cần cái này để hiện thông báo xanh/đỏ
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
  currentUser: UserResponse | null = null;
  userMenuItems: MenuItem[] = [];

  // --- Profile Logic ---
  profileDialog: boolean = false;
  profileForm: FormGroup;
  isUploading: boolean = false;
  uploadedImageUrl: string = '';
  genderOptions = [{ label: 'Nam', value: 'MALE' }, { label: 'Nữ', value: 'FEMALE' }];

  historyDialog: boolean = false;
  myAppointments: AppointmentResponse[] = [];
  loadingHistory: boolean = false;

  // Injects
  private userService = inject(UserService);
  private authService = inject(AuthService);
  private uploadService = inject(UploadService);
  private appointmentService = inject(AppointmentService);
  private messageService = inject(MessageService);
  private router = inject(Router);
  private fb = inject(FormBuilder);

  constructor() {
    // Khởi tạo form
    this.profileForm = this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(5)]],
      email: [{ value: '', disabled: true }], // Email không cho sửa
      phoneNumber: ['', [Validators.pattern(/(84|0[3|5|7|8|9])+([0-9]{8})\b/)]],
      dateOfBirth: [null],
      gender: [null],
      address: ['']
    });
  }

  ngOnInit() {
    this.checkLoginStatus();
  }

  checkLoginStatus() {
    this.userService.getMyInfo().subscribe({
      next: (res) => {
        if (res.result) {
          this.currentUser = res.result;
          this.initUserMenu(); // Có user rồi mới tạo menu
        }
      },
      error: () => {
        this.currentUser = null;
      }
    });
  }

  initUserMenu() {
    this.userMenuItems = [
      {
        label: 'Hồ sơ cá nhân',
        icon: 'pi pi-user',
        command: () => this.openProfileDialog() // <--- GỌI HÀM NÀY
      },
      {
        label: 'Lịch sử đặt khám', // <--- MỤC MENU MỚI
        icon: 'pi pi-calendar',
        command: () => this.openHistoryDialog()
      },
      { separator: true },
      {
        label: 'Đăng xuất',
        icon: 'pi pi-sign-out',
        styleClass: 'text-red-500',
        command: () => this.logout()
      }
    ];
  }

  // --- MỞ DIALOG & FILL DỮ LIỆU ---
  openProfileDialog() {
    if (!this.currentUser) return;

    this.profileForm.patchValue({
      fullName: this.currentUser.fullName,
      email: this.currentUser.email,
      phoneNumber: this.currentUser.phoneNumber,
      address: this.currentUser.address,
      gender: this.currentUser.gender,
      dateOfBirth: this.currentUser.dateOfBirth ? new Date(this.currentUser.dateOfBirth) : null
    });

    this.uploadedImageUrl = this.currentUser.image || '';
    this.profileDialog = true;
  }

  openHistoryDialog() {
    this.historyDialog = true;
    this.loadMyAppointments();
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

  // --- UPLOAD ẢNH ---
  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      this.isUploading = true;
      this.uploadService.uploadImage(file).subscribe({
        next: (res) => {
          if (res.code === 1000) {
            this.uploadedImageUrl = res.result;
            this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã tải ảnh lên!' });
          }
          this.isUploading = false;
        },
        error: () => {
          this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Upload ảnh thất bại' });
          this.isUploading = false;
        }
      });
    }
  }

  // --- LƯU THÔNG TIN ---
  saveProfile() {
    if (this.profileForm.invalid) return;

    const formValue = this.profileForm.getRawValue();
    const formattedDob = formValue.dateOfBirth ? formatDate(formValue.dateOfBirth, 'yyyy-MM-dd', 'en-US') : null;

    const updateData = {
      fullName: formValue.fullName,
      phoneNumber: formValue.phoneNumber,
      gender: formValue.gender,
      dateOfBirth: formattedDob,
      address: formValue.address,
      image: this.uploadedImageUrl
    };

    this.userService.updateMyInfo(updateData as any).subscribe({
      next: (res) => {
        this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Cập nhật hồ sơ thành công!' });
        this.profileDialog = false;
        if (res.result) this.currentUser = res.result; // Update UI ngay lập tức
      },
      error: (err) => {
        this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Cập nhật thất bại' });
      }
    });
  }

  logout() {
    this.authService.logout().subscribe({
        next: () => console.log('Logout server success'),
        error: (err) => console.warn('Logout server failed or token expired', err)
    });

    this.handleLogoutSuccess();
  }

  private handleLogoutSuccess() {
    localStorage.removeItem('token');
    this.currentUser = null;
    this.router.navigate(['/login']);
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
}
