import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, formatDate } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { UserResponse } from '../../../models/user.model';
import { UserService } from '../../../core/services/user.service';

// PrimeNG v17 Modules
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { TagModule } from 'primeng/tag';
import { DropdownModule } from 'primeng/dropdown';
import { CalendarModule } from 'primeng/calendar';
import { MultiSelectModule } from 'primeng/multiselect'; // Để chọn nhiều Role
import { ToolbarModule } from 'primeng/toolbar';
import { ConfirmationService, MessageService } from 'primeng/api';

@Component({
  selector: 'app-user-management',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, FormsModule,
    TableModule, ButtonModule, InputTextModule,
    DialogModule, ToastModule, ConfirmDialogModule,
    TagModule, DropdownModule, CalendarModule,
    MultiSelectModule, ToolbarModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.scss']
})
export class UserManagementComponent implements OnInit {
  // Data
  users: UserResponse[] = [];
  totalRecords: number = 0;
  loading: boolean = false;

  // Pagination Params
  page: number = 1;
  size: number = 10;
  keyword: string = '';
  selectedStatus: boolean | null = null;
  selectedRole: string | null = null;

  // Dialog & Form
  userDialog: boolean = false;
  userForm: FormGroup;
  submitted: boolean = false;
  isEditMode: boolean = false; // Phân biệt Thêm mới vs Sửa

  // Options
  genderOptions = [{ label: 'Nam', value: 'MALE' }, { label: 'Nữ', value: 'FEMALE' }];
  // Role string cho UserUpdateRequest (List<String>)
  roleOptions = [
    { label: 'Admin', value: 'ADMIN' },
    { label: 'Doctor', value: 'DOCTOR' },
    { label: 'Receptionist', value: 'RECEPTIONIST' },
    { label: 'Patient', value: 'PATIENT' }
  ];
  statusOptions = [
      { label: 'Tất cả trạng thái', value: null },
      { label: 'Đang hoạt động', value: true },
      { label: 'Đã vô hiệu', value: false }
  ];
  formStatusOptions = [
    { label: 'Hoạt động', value: true },
    { label: 'Vô hiệu', value: false }
  ];
  filterRoleOptions = [
      { label: 'Tất cả vai trò', value: null },
      { label: 'Quản trị viên (Admin)', value: 'ADMIN' },
      { label: 'Bác sĩ (Doctor)', value: 'DOCTOR' },
      { label: 'Lễ tân (Receptionist)', value: 'RECEPTIONIST' },
      { label: 'Bệnh nhân (Patient)', value: 'PATIENT' }
  ];

  private userService = inject(UserService);
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);
  private fb = inject(FormBuilder);

  constructor() {
    this.userForm = this.fb.group({
      userId: [null],
      fullName: ['', [Validators.required, Validators.minLength(5)]],
      email: ['', [Validators.required, Validators.email]],
      password: [''], // Required khi tạo mới, Optional khi sửa
      phoneNumber: ['', [Validators.pattern(/(84|0[3|5|7|8|9])+([0-9]{8})\b/)]],
      address: [''],
      gender: [null, Validators.required],
      dateOfBirth: [null, Validators.required],
      roles: [[]],
      isActive: [true]
    });
  }

  ngOnInit() {
    this.loadUsers();
  }

  // 1. Load danh sách (Server-side pagination)
  loadUsers(event?: any) {
    this.loading = true;

    // Nếu gọi từ bảng (khi chuyển trang)
    if (event) {
      this.page = (event.first / event.rows) + 1;
      this.size = event.rows;
    }

    this.userService.getUsers(this.page, this.size,this.selectedStatus, this.selectedRole, this.keyword).subscribe({
        next: (res) => {
            this.users = res.result?.data || [];
            this.totalRecords = res.result?.totalElements || 0;
            this.loading = false;
        },
        error: () => this.loading = false
    });
  }

  // 2. Mở Dialog Thêm mới
  openNew() {
    this.isEditMode = false;
    this.userForm.reset();

    // Khi tạo mới, password là bắt buộc (theo UserCreationRequest)
    this.userForm.get('password')?.setValidators([Validators.required, Validators.minLength(8)]);
    this.userForm.get('password')?.updateValueAndValidity();

    this.submitted = false;
    this.userDialog = true;
  }

  // 3. Mở Dialog Sửa
  editUser(user: UserResponse) {
    this.isEditMode = true;
    const roleNames = user.roles ? user.roles.map(r => r.name) : [];

    this.userForm.patchValue({
      ...user,
      dateOfBirth: user.dateOfBirth ? new Date(user.dateOfBirth) : null,
      roles: roleNames,
      isActive: user.isActive
    });

    this.userForm.get('password')?.clearValidators();
    this.userForm.get('password')?.updateValueAndValidity();

    this.userDialog = true;
  }

  saveUser() {
    this.submitted = true;
    if (this.userForm.invalid) return;

    const formValue = this.userForm.value;

    const formattedDob = formValue.dateOfBirth ? formatDate(formValue.dateOfBirth, 'yyyy-MM-dd', 'en-US') : null;

    if (this.isEditMode) {
      // --- LOGIC UPDATE (UserUpdateRequest) ---
      const updateData = {
        fullName: formValue.fullName,
        password: formValue.password || null,
        phoneNumber: formValue.phoneNumber,
        gender: formValue.gender,
        dateOfBirth: formattedDob,
        address: formValue.address,
        roles: formValue.roles,
        isActive: formValue.isActive
      };

      this.userService.updateUser(formValue.userId, updateData as any).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật người dùng' });
          this.hideDialog();
          this.loadUsers();
        },
        error: (err) => this.showError(err)
      });

    } else {
      // --- LOGIC CREATE (UserCreationRequest) ---
      // Lưu ý: UserCreationRequest KHÔNG có roles (Mặc định là USER)
      const createData = {
        fullName: formValue.fullName,
        email: formValue.email,
        password: formValue.password,
        phoneNumber: formValue.phoneNumber,
        address: formValue.address,
        gender: formValue.gender,
        dateOfBirth: formattedDob
      };

      this.userService.register(createData as any).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã tạo người dùng mới' });
          this.hideDialog();
          this.loadUsers();
        },
        error: (err) => this.showError(err)
      });
    }
  }

  deleteUser(user: UserResponse) {
    // 1. Kiểm tra Logic: Nếu User đã bị vô hiệu hóa (isActive = false) thì không cần xóa nữa
    if (!user.isActive) {
      this.messageService.add({
        severity: 'warn',
        summary: 'Cảnh báo',
        detail: 'Tài khoản này đã bị vô hiệu hóa từ trước.'
      });
      return;
    }

    // 2. Cập nhật thông báo Confirm Dialog cho đúng nghiệp vụ Soft Delete
    this.confirmationService.confirm({
      message: `Bạn có chắc chắn muốn <b>vô hiệu hóa</b> tài khoản <b>${user.email}</b>?<br>Người dùng sẽ không thể đăng nhập hệ thống.`,
      header: 'Xác nhận vô hiệu hóa', // Đổi từ "Xác nhận xóa"
      icon: 'pi pi-lock', // Đổi icon từ cảnh báo sang icon khóa cho hợp ngữ cảnh
      acceptLabel: 'Vô hiệu hóa',
      rejectLabel: 'Hủy',
      acceptButtonStyleClass: 'p-button-danger',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        this.userService.deleteUser(user.userId).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Thành công',
              detail: 'Đã vô hiệu hóa tài khoản người dùng' // Thông báo chính xác
            });
            this.loadUsers(); // Tải lại bảng để cập nhật trạng thái (Active -> Locked)
          },
          error: (err) => this.showError(err)
        });
      }
    });
  }

  hideDialog() {
    this.userDialog = false;
    this.submitted = false;
  }

  showError(err: any) {
    const msg = err.error?.message || 'Có lỗi xảy ra!';
    this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: msg });
  }

  getRoleSeverity(roleName: string) {
    if (roleName === 'ADMIN') return 'danger';
    if (roleName === 'DOCTOR' || roleName === 'RECEPTIONIST') return 'info';
    return 'success';
  }

  // Trong user-management.component.ts

  exportExcel() {
    this.loading = true;

    // Gọi hàm export không tham số
    this.userService.exportUsers().subscribe({
        next: (blob: Blob) => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;

            // Đặt tên file đơn giản
            a.download = `users_full_list.xlsx`;

            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);

            this.loading = false;
            this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xuất toàn bộ danh sách' });
        },
        error: (err) => {
            this.loading = false;
            this.showError(err);
        }
    });
  }
}
