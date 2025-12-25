import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';

// Services
import { StaffService } from '../../../../core/services/staff.service';
import { UserService } from '../../../../core/services/user.service';
import { MasterDataService } from '../../../../core/services/master-data.service'; //

// Models
import { DoctorResponse } from '../../../../models/staff.model';

// PrimeNG Modules
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { DropdownModule } from 'primeng/dropdown';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { MessageService, ConfirmationService } from 'primeng/api';

@Component({
  selector: 'app-doctor-management',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, FormsModule,
    TableModule, ButtonModule, InputTextModule, DialogModule,
    DropdownModule, ToastModule, ConfirmDialogModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './doctor-management.component.html',
  styleUrls: ['./doctor-management.component.scss']
})
export class DoctorManagementComponent implements OnInit {
  doctors: DoctorResponse[] = [];
  availableUsers: any[] = [];
  specialties: any[] = []; // Dữ liệu cho Dropdown Chuyên khoa

  totalRecords: number = 0;
  loading: boolean = false;
  page: number = 1;
  size: number = 10;
  keyword: string = '';

  doctorDialog: boolean = false;
  doctorForm: FormGroup;
  submitted: boolean = false;
  isEditMode: boolean = false;

  // Dependency Injection
  private staffService = inject(StaffService);
  private userService = inject(UserService);
  private masterDataService = inject(MasterDataService); // Sử dụng service có sẵn
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);
  private fb = inject(FormBuilder);

  constructor() {
    this.doctorForm = this.fb.group({
      doctorId: [null],
      userId: [null],
      specialtyId: [null, Validators.required],
      fullName: [{value: '', disabled: true}]
    });
  }

  ngOnInit() {
    this.loadDoctors();
    this.loadSpecialties();
    this.loadAvailableUsers();
  }

  loadDoctors(event?: any) {
    this.loading = true;
    if (event) {
      this.page = (event.first / event.rows) + 1;
      this.size = event.rows;
    }

    this.staffService.getAllDoctors(this.page, this.size, this.keyword).subscribe({
      next: (res) => {
        this.doctors = res.result?.data || [];
        this.totalRecords = res.result?.totalElements || 0;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  // Sử dụng MasterDataService để lấy chuyên khoa
  loadSpecialties() {
    this.masterDataService.getAllSpecialties(1, 100).subscribe(res => {
      this.specialties = res.result?.data.map((s: any) => ({
        label: s.name, // Hiển thị tên khoa
        value: s.specialtyId    // Giá trị là ID
      })) || [];
    });
  }

  loadAvailableUsers() {
    this.userService.getUsers(1, 1000, true, '').subscribe(res => {
      const allUsers = res.result?.data || [];

      const onlyPatients = allUsers.filter((u: any) => {
        if (!u.roles || !Array.isArray(u.roles)) return false;

        return u.roles.some((r: any) => r.name === 'PATIENT');
      });

      this.availableUsers = onlyPatients.map((u: any) => ({
        label: `${u.fullName} (${u.email})`,
        value: u.userId
      }));

      console.log('Đã lọc được số lượng Patient:', this.availableUsers.length);
    });
  }

  openNew() {
    this.isEditMode = false;
    this.doctorForm.reset();
    this.doctorForm.get('userId')?.setValidators(Validators.required);
    this.submitted = false;
    this.doctorDialog = true;
  }

  editDoctor(doc: DoctorResponse) {
    this.isEditMode = true;
    this.doctorForm.patchValue({
      doctorId: doc.doctorId,
      specialtyId: doc.specialtyId,
      fullName: doc.fullName
    });
    this.doctorForm.get('userId')?.clearValidators();
    this.doctorDialog = true;
  }

  saveDoctor() {
    this.submitted = true;
    if (this.doctorForm.invalid) return;

    const val = this.doctorForm.getRawValue();

    if (this.isEditMode) {
      this.staffService.updateDoctor(val.doctorId, {
        specialtyId: val.specialtyId
      }).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật thông tin' });
          this.doctorDialog = false;
          this.loadDoctors();
        }
      });
    } else {
      this.staffService.createDoctor({
        userId: val.userId,
        specialtyId: val.specialtyId
      }).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã thêm bác sĩ mới' });
          this.doctorDialog = false;
          this.loadDoctors();
        }
      });
    }
  }

  deleteDoctor(id: number) {
    this.confirmationService.confirm({
      message: 'Bạn có chắc chắn muốn <b>dừng hoạt động</b> bác sĩ này?<br>Tài khoản đăng nhập sẽ bị khóa, nhưng dữ liệu hồ sơ vẫn được lưu trữ.',
      header: 'Xác nhận vô hiệu hóa',
      icon: 'pi pi-user-minus', // Icon thể hiện gỡ bỏ người dùng
      acceptLabel: 'Đồng ý',
      rejectLabel: 'Hủy',
      acceptButtonStyleClass: 'p-button-danger',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        this.staffService.deleteDoctor(id).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Thành công',
              detail: 'Đã vô hiệu hóa quyền truy cập của bác sĩ'
            });
            this.loadDoctors(); // Tải lại danh sách
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
}
