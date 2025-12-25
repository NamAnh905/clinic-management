import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, formatDate } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { PatientService } from '../../../core/services/patient.service';
import { PatientUpdationRequest, PatientResponse } from '../../../models/patient.model';

import { ConfirmationService, MessageService } from 'primeng/api';
import { TooltipModule } from 'primeng/tooltip';

// PrimeNG Modules
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DropdownModule } from 'primeng/dropdown';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { CalendarModule } from 'primeng/calendar';
import { TagModule } from 'primeng/tag';

@Component({
  selector: 'app-patient-management',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, FormsModule,
    TableModule, ButtonModule, InputTextModule, DialogModule,
    ToastModule, ConfirmDialogModule, DropdownModule,
    InputTextareaModule, CalendarModule, TagModule,
    TooltipModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './patient-management.component.html',
  styleUrls: ['./patient-management.component.scss']
})
export class PatientManagementComponent implements OnInit {
  patients: PatientResponse[] = [];
  totalRecords: number = 0;
  loading: boolean = false;
  keyword: string = '';
  page: number = 1;
  size: number = 10;

  patientDialog: boolean = false;
  patientForm: FormGroup;
  genderOptions = [
    { label: 'Nam', value: 'MALE' },
    { label: 'Nữ', value: 'FEMALE' },
    { label: 'Khác', value: 'OTHER' }
  ];

  private patientService = inject(PatientService);
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);
  private fb = inject(FormBuilder);

  constructor() {
    this.patientForm = this.fb.group({
      patientId: [null],
      fullName: ['', Validators.required], // Ánh xạ sang User.fullName ở BE
      phoneNumber: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]],
      gender: ['MALE', Validators.required],
      dateOfBirth: [null, Validators.required],
      address: [''],
      medicalHistory: ['']
    });
  }

  ngOnInit() {
    this.loadPatients();
  }

  loadPatients(event?: any) {
    this.loading = true;
    if (event) {
      this.page = (event.first / event.rows) + 1;
      this.size = event.rows;
    }
    this.patientService.getPatients(this.page, this.size, this.keyword).subscribe({
      next: (res) => {
        this.patients = res.result?.data || [];
        this.totalRecords = res.result?.totalElements || 0;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  editPatient(patient: PatientResponse) {
    this.patientForm.patchValue({
      patientId: patient.patientId,
      fullName: patient.patientName, // Map từ PatientResponse.patientName
      phoneNumber: patient.phoneNumber,
      gender: patient.gender,
      dateOfBirth: patient.dateOfBirth ? new Date(patient.dateOfBirth) : null,
      address: patient.address,
      medicalHistory: patient.medicalHistory
    });
    this.patientDialog = true;
  }

  savePatient() {
    if (this.patientForm.invalid) return;

    const val = this.patientForm.value;
    // Chuẩn bị DTO đúng theo PatientUpdationRequest ở Backend
    const updateRequest: PatientUpdationRequest = {
      fullName: val.fullName,
      phoneNumber: val.phoneNumber,
      gender: val.gender,
      dateOfBirth: val.dateOfBirth ? formatDate(val.dateOfBirth, 'yyyy-MM-dd', 'en-US') : undefined,
      address: val.address,
      medicalHistory: val.medicalHistory
    };

    this.patientService.updatePatient(val.patientId, updateRequest).subscribe({
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật thông tin bệnh nhân' });
        this.patientDialog = false;
        this.loadPatients();
      },
      error: (err) => this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: err.error?.message })
    });
  }

  deletePatient(patient: PatientResponse) {
    this.confirmationService.confirm({
      message: `Bạn có chắc chắn muốn <b>vô hiệu hóa</b> tài khoản của bệnh nhân <b>${patient.patientName}</b>?`,
      header: 'Xác nhận vô hiệu hóa',
      icon: 'pi pi-user-minus',
      acceptLabel: 'Đồng ý',
      rejectLabel: 'Hủy',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        // Gọi DELETE để vô hiệu hóa account qua UserService ở BE
        this.patientService.deletePatient(patient.patientId).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Tài khoản đã bị vô hiệu hóa' });
            this.loadPatients();
          },
          error: (err) => this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: err.error?.message })
        });
      }
    });
  }
}
