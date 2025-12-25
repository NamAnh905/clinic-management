import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, formatDate } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';

// PrimeNG
import { ButtonModule } from 'primeng/button';
import { CalendarModule } from 'primeng/calendar';
import { DropdownModule } from 'primeng/dropdown';
import { RadioButtonModule } from 'primeng/radiobutton';
import { InputTextModule } from 'primeng/inputtext';
import { TooltipModule } from 'primeng/tooltip';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { MessageService, ConfirmationService } from 'primeng/api';

import { ScheduleService } from '../../../core/services/schedule.service';
import { MasterDataService } from '../../../core/services/master-data.service';
import { StaffService } from '../../../core/services/staff.service';
import { AuthService } from '../../../core/services/auth.service';
import { ScheduleResponse } from '../../../models/schedule.model';

@Component({
  selector: 'app-schedule-management',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule,
    ButtonModule, CalendarModule, DropdownModule, InputTextModule,
    TooltipModule, DialogModule, ToastModule, ConfirmDialogModule,
    RadioButtonModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './schedule-management.component.html',
  styleUrls: ['./schedule-management.component.scss']
})
export class ScheduleManagementComponent implements OnInit {
  // Cấu hình khung giờ (7h sáng -> 22h tối)
  startHour = 7;
  endHour = 22;

  currentDate: Date = new Date();
  timeSlots: number[] = [];
  rawSchedules: ScheduleResponse[] = [];
  specialties: any[] = [];

  // Filters & Data
  allDoctors: any[] = []; // List gốc
  doctors: any[] = [];    // List hiển thị (có thể bị filter theo khoa)
  receptionists: any[] = [];

  dialogDoctors: any[] = []; // List dùng cho dialog
  dialogSpecialtyId: number | null = null;
  selectedSpecialtyId: number | null = null;

  // UI State
  scheduleDialog: boolean = false;
  scheduleForm: FormGroup;
  submitted: boolean = false;
  loading: boolean = false;
  isEditMode: boolean = false;
  currentScheduleId: number | null = null;

  currentUser: any = null;
  currentDoctorId: number | null = null;

  // View Mode
  viewOptions = [
      { label: 'Bác sĩ', value: 'DOCTOR' },
      { label: 'Lễ tân', value: 'RECEPTIONIST' }
  ];
  viewMode: 'DOCTOR' | 'RECEPTIONIST' = 'DOCTOR';

  // Create Mode
  targetType: 'DOCTOR' | 'RECEPTIONIST' = 'DOCTOR';

  private scheduleService = inject(ScheduleService);
  private masterDataService = inject(MasterDataService);
  private staffService = inject(StaffService);
  public authService = inject(AuthService);
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);
  private fb = inject(FormBuilder);

  get isDoctor(): boolean { return this.authService.isDoctor; }
  get isAdmin(): boolean { return this.authService.isAdmin; }
  get isReceptionist(): boolean { return this.authService.isReceptionist; }

  // [MỚI] Getter để lấy danh sách nhân viên hiển thị bên trái bảng Gantt
  get displayedStaff(): any[] {
      if (this.viewMode === 'DOCTOR') {
          // Nếu có chọn chuyên khoa thì lọc bác sĩ theo chuyên khoa
          if (this.selectedSpecialtyId) {
              return this.allDoctors.filter(d => d.specialtyId === this.selectedSpecialtyId);
          }
          return this.allDoctors;
      } else {
          return this.receptionists;
      }
  }

  constructor() {
    this.scheduleForm = this.fb.group({
      doctorId: [null, Validators.required],
      workDate: [null, Validators.required],
      startTime: [null, Validators.required],
      endTime: [null, Validators.required]
    });
  }

  ngOnInit() {
      this.generateTimeSlots();
      this.loadMetaData();

      // 1. Subscribe User & Role
      this.authService.currentUser$.subscribe(user => {
          this.currentUser = user;
          this.loadDoctors();

          if (this.isAdmin || !this.isDoctor) {
              this.loadReceptionists();
          }

          // Logic ép viewMode
          if (this.isDoctor) {
              this.viewMode = 'DOCTOR';
          }
      });

      // 2. Subscribe Doctor ID & Load Lịch
      this.authService.currentDoctorId$.subscribe(id => {
          this.currentDoctorId = id;
          this.loadSchedules();
      });
  }

  generateTimeSlots() {
    this.timeSlots = [];
    for (let i = this.startHour; i <= this.endHour; i++) {
      this.timeSlots.push(i);
    }
  }

  loadMetaData() {
    this.masterDataService.getAllSpecialties(1, 100).subscribe(res => {
      this.specialties = res.result?.data.map((s: any) => ({
        label: s.name,
        value: s.specialtyId
      })) || [];
    });
  }

  loadDoctors() {
    this.staffService.getAllDoctors(1, 1000).subscribe(res => {
      const list = res.result?.data.map((d: any) => ({
        label: `${d.fullName}`,
        value: d.doctorId,
        specialtyId: d.specialtyId,
        avatar: d.fullName.charAt(0) // Tạo avatar chữ cái đầu
      })) || [];
      this.allDoctors = list;
      this.dialogDoctors = list;
    });
  }

  loadReceptionists() {
      this.staffService.getAllReceptionists(1, 1000).subscribe(res => {
          this.receptionists = res.result?.data.map((r: any) => ({
              label: `${r.fullName} (${r.employeeCode})`,
              value: r.receptionistId,
              avatar: r.fullName.charAt(0)
          })) || [];
      });
  }

  onDialogSpecialtyChange() {
    if (this.dialogSpecialtyId) {
        this.dialogDoctors = this.allDoctors.filter(d => d.specialtyId === this.dialogSpecialtyId);
    } else {
        this.dialogDoctors = this.allDoctors;
    }
    this.scheduleForm.patchValue({ doctorId: null });
  }

  // [SỬA] Load dữ liệu chỉ cho ngày hiện tại (CurrentDate)
  loadSchedules() {
    this.loading = true;
    // Chuyển sang xem theo NGÀY, nên start = end = currentDate
    const dateStr = formatDate(this.currentDate, 'yyyy-MM-dd', 'en-US');

    const requestViewType = this.isDoctor ? 'DOCTOR' : this.viewMode;
    let filterDocId = undefined;
    let filterRecId = undefined;

    this.scheduleService.getSchedules(
        1, 1000,
        filterDocId,
        filterRecId,
        this.selectedSpecialtyId || undefined,
        dateStr, // Start Date
        dateStr, // End Date
        requestViewType
    ).subscribe({
      next: (res) => {
        this.rawSchedules = res.result?.data || [];
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  // [MỚI] Lọc sự kiện cho từng nhân viên cụ thể trong ngày
  getEventsForStaff(staffId: number): ScheduleResponse[] {
      return this.rawSchedules.filter(s => {
          if (this.viewMode === 'DOCTOR') return s.doctorId === staffId;
          else return s.receptionistId === staffId;
      });
  }

  // [MỚI] Tính toán style cho thanh Gantt (Ngang)
  calculateGanttStyle(schedule: ScheduleResponse) {
      const totalHours = this.endHour - this.startHour + 1; // +1 để hiển thị cột cuối cùng

      // Parse giờ
      const startParts = schedule.startTime.toString().split(':');
      const endParts = schedule.endTime.toString().split(':');

      // Đổi ra số thập phân (VD: 8:30 -> 8.5)
      const startVal = parseInt(startParts[0]) + (parseInt(startParts[1]) / 60);
      const endVal = parseInt(endParts[0]) + (parseInt(endParts[1]) / 60);

      // Tính vị trí trái (Left %) và độ rộng (Width %)
      // Left = (Giờ bắt đầu - Giờ mở cửa) / Tổng giờ * 100
      const leftPercent = ((startVal - this.startHour) / totalHours) * 100;
      const widthPercent = ((endVal - startVal) / totalHours) * 100;

      // Màu sắc
      let isMySchedule = (this.isDoctor && schedule.doctorId === this.currentDoctorId);
      let bgColor = '#f1f5f9';
      let borderColor = '#64748b';

      if (schedule.doctorId) {
          bgColor = isMySchedule ? '#eff6ff' : '#dbeafe'; // Xanh
          borderColor = isMySchedule ? '#2563eb' : '#3b82f6';
      } else if (schedule.receptionistId) {
          bgColor = '#fff7ed'; // Cam
          borderColor = '#f97316';
      }

      return {
          'left.%': leftPercent,
          'width.%': widthPercent,
          'background-color': bgColor,
          'border-left': `4px solid ${borderColor}`,
          'position': 'absolute',
          'height': '36px',
          'top': '50%',
          'transform': 'translateY(-50%)',
          'border-radius': '4px',
          'font-size': '0.75rem',
          'overflow': 'hidden',
          'white-space': 'nowrap',
          'z-index': '10',
          'cursor': (this.isAdmin || isMySchedule) ? 'pointer' : 'default',
          'display': 'flex',
          'align-items': 'center',
          'padding': '0 8px',
          'box-shadow': '0 2px 4px rgba(0,0,0,0.05)'
      };
  }

  // --- ACTIONS (Create/Edit/Delete) giữ nguyên logic cũ ---

  openNew() {
    this.isEditMode = false;
    this.currentScheduleId = null;
    this.scheduleForm.reset();
    this.targetType = 'DOCTOR';
    this.scheduleForm.get('doctorId')?.enable();
    this.dialogDoctors = this.allDoctors;
    this.dialogSpecialtyId = null;

    // Set time default
    const today = new Date(this.currentDate); // Lấy ngày đang chọn làm mặc định
    const start = new Date(); start.setHours(8, 0, 0);
    const end = new Date(); end.setHours(17, 0, 0);
    this.scheduleForm.patchValue({ workDate: today, startTime: start, endTime: end });

    this.scheduleDialog = true;
  }

  openEdit(schedule: ScheduleResponse) {
    if (!this.isAdmin && schedule.doctorId !== this.currentDoctorId) return;

    this.isEditMode = true;
    this.currentScheduleId = schedule.scheduleId;
    this.scheduleForm.reset();
    this.scheduleForm.get('doctorId')?.disable(); // Không cho sửa người

    const dateParts = schedule.workDate.split('-');
    const workDate = new Date(+dateParts[0], +dateParts[1] - 1, +dateParts[2]);

    const startParts = schedule.startTime.toString().split(':');
    const endParts = schedule.endTime.toString().split(':');
    const startTime = new Date(); startTime.setHours(+startParts[0], +startParts[1], +startParts[2] || 0);
    const endTime = new Date(); endTime.setHours(+endParts[0], +endParts[1], +endParts[2] || 0);

    this.scheduleForm.patchValue({
      doctorId: schedule.doctorId || schedule.receptionistId, // Map ID vào form
      workDate: workDate,
      startTime: startTime,
      endTime: endTime
    });

    this.targetType = schedule.doctorId ? 'DOCTOR' : 'RECEPTIONIST';
    this.scheduleDialog = true;
  }

  saveSchedule() {
      this.submitted = true;
      if (this.scheduleForm.invalid) return;

      const val = this.scheduleForm.getRawValue();
      if (val.startTime >= val.endTime) {
        this.messageService.add({ severity: 'warn', summary: 'Cảnh báo', detail: 'Giờ kết thúc phải sau giờ bắt đầu' });
        return;
      }

      const payload: any = {
        workDate: formatDate(val.workDate, 'yyyy-MM-dd', 'en-US'),
        startTime: formatDate(val.startTime, 'HH:mm:ss', 'en-US'),
        endTime: formatDate(val.endTime, 'HH:mm:ss', 'en-US')
      };

      if (this.isEditMode && this.currentScheduleId) {
        this.scheduleService.updateSchedule(this.currentScheduleId, payload).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Cập nhật lịch thành công' });
            this.scheduleDialog = false;
            this.loadSchedules();
          },
          error: (err) => this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: err.error?.message })
        });
      } else {
        if (this.targetType === 'DOCTOR') {
            payload.doctorId = val.doctorId;
            payload.receptionistId = null;
        } else {
            payload.doctorId = null;
            payload.receptionistId = val.doctorId;
        }

        this.scheduleService.createSchedule(payload).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Tạo lịch mới thành công' });
            this.scheduleDialog = false;
            this.loadSchedules();
          },
          error: (err) => this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: err.error?.message })
        });
      }
  }

  deleteSchedule() {
    if (!this.currentScheduleId) return;
    this.confirmationService.confirm({
      message: 'Bạn có chắc chắn muốn xóa ca trực này không?',
      header: 'Xác nhận xóa',
      icon: 'pi pi-trash',
      acceptLabel: 'Xóa',
      acceptButtonStyleClass: 'p-button-danger',
      rejectLabel: 'Hủy',
      accept: () => {
        this.scheduleService.deleteSchedule(this.currentScheduleId!).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa lịch làm việc' });
            this.scheduleDialog = false;
            this.loadSchedules();
          },
          error: (err) => this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: err.error?.message })
        });
      }
    });
  }

  onTargetTypeChange() {
      this.scheduleForm.patchValue({ doctorId: null });
  }
}
