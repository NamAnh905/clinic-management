import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, formatDate } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { StaffService } from '../../../../core/services/staff.service';
import { UserService } from '../../../../core/services/user.service';
import { ReceptionistResponse } from '../../../../models/staff.model';

// PrimeNG Modules (Giữ nguyên như user-management)
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { CalendarModule } from 'primeng/calendar';
import { DropdownModule } from 'primeng/dropdown';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { MessageService, ConfirmationService } from 'primeng/api';

@Component({
  selector: 'app-receptionist-management',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, FormsModule,
    TableModule, ButtonModule, InputTextModule, DialogModule,
    CalendarModule, DropdownModule, ToastModule, ConfirmDialogModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './receptionist-management.component.html',
  styleUrls: ['./receptionist-management.component.scss'] // Dùng chung SCSS đã tối ưu
})
export class ReceptionistManagementComponent implements OnInit {
  receptionists: ReceptionistResponse[] = [];
  availableUsers: any[] = []; // Danh sách user để chọn làm lễ tân
  totalRecords: number = 0;
  loading: boolean = false;

  page: number = 1;
  size: number = 10;
  keyword: string = '';

  receptionistDialog: boolean = false;
  staffForm: FormGroup;
  submitted: boolean = false;
  isEditMode: boolean = false;

  private staffService = inject(StaffService);
  private userService = inject(UserService);
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);
  private fb = inject(FormBuilder);

  constructor() {
    this.staffForm = this.fb.group({
      receptionistId: [null],
      userId: [null], // Chỉ dùng khi tạo mới
      employeeCode: [{value: '', disabled: true}], // Backend tự sinh
      hireDate: [new Date(), Validators.required] //
    });
  }

  ngOnInit() {
    this.loadReceptionists();
    this.loadAvailableUsers();
  }

  loadReceptionists(event?: any) {
    this.loading = true; // Bật loading
    if (event) {
      this.page = (event.first / event.rows) + 1;
      this.size = event.rows;
    }

    this.staffService.getAllReceptionists(this.page, this.size, this.keyword).subscribe({
      next: (res) => {
        this.receptionists = res.result?.data || [];
        this.totalRecords = res.result?.totalElements || 0;
        this.loading = false; // Tắt loading
      },
      error: () => this.loading = false
    });
  }

  loadAvailableUsers() {
    this.userService.getUsers(1, 1000, true, '').subscribe(res => {
      const allUsers = res.result?.data || [];

      const candidates = allUsers.filter((u: any) => {
        if (!u.roles || !Array.isArray(u.roles)) return false;

        return u.roles.some((r: any) => r.name === 'PATIENT');
      });

      this.availableUsers = candidates.map((u: any) => ({
        label: `${u.fullName} (${u.email})`,
        value: u.userId
      }));

      console.log('Số lượng ứng viên cho vị trí Lễ tân:', this.availableUsers.length);
    });
  }

  openNew() {
    this.isEditMode = false;
    this.staffForm.reset({ hireDate: new Date() });
    this.staffForm.get('userId')?.setValidators(Validators.required);
    this.submitted = false;
    this.receptionistDialog = true;
  }

  editStaff(staff: ReceptionistResponse) {
    this.isEditMode = true;
    this.staffForm.patchValue({
      receptionistId: staff.receptionistId,
      employeeCode: staff.employeeCode,
      hireDate: staff.hireDate ? new Date(staff.hireDate) : null
    });
    this.staffForm.get('userId')?.clearValidators(); // Khi sửa không cần userId
    this.receptionistDialog = true;
  }

  deleteReceptionist(id: number) {
    this.confirmationService.confirm({
      message: 'Bạn có chắc chắn muốn <b>dừng hoạt động</b> nhân viên lễ tân này?<br>Tài khoản đăng nhập sẽ bị khóa, nhưng dữ liệu hồ sơ vẫn được lưu trữ.',
      header: 'Xác nhận vô hiệu hóa',
      icon: 'pi pi-user-minus', // Icon thể hiện việc gỡ bỏ người dùng
      acceptLabel: 'Đồng ý',
      rejectLabel: 'Hủy',
      acceptButtonStyleClass: 'p-button-danger',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        // Gọi xuống Service
        this.staffService.deleteReceptionist(id).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Thành công',
              detail: 'Đã vô hiệu hóa quyền truy cập của lễ tân'
            });
            this.loadReceptionists();
          },
          error: (err) => {
            this.messageService.add({
              severity: 'error',
              summary: 'Lỗi',
              detail: 'Không thể thực hiện thao tác (Vui lòng thử lại)'
            });
          }
        });
      }
    });
  }

  saveStaff() {
    this.submitted = true;
    if (this.staffForm.invalid) return;

    const val = this.staffForm.getRawValue();
    const hireDateStr = formatDate(val.hireDate, 'yyyy-MM-dd', 'en-US');

    if (this.isEditMode) {
      this.staffService.updateReceptionist(val.receptionistId, { hireDate: hireDateStr }).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Cập nhật lễ tân thành công' });
          this.receptionistDialog = false;
          this.loadReceptionists();
        }
      });
    } else {
      this.staffService.createReceptionist({ userId: val.userId, hireDate: hireDateStr }).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã bổ nhiệm lễ tân mới' });
          this.receptionistDialog = false;
          this.loadReceptionists();
        }
      });
    }
  }
}
