import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';

// Services
import { MasterDataService } from '../../../../core/services/master-data.service';
import { SpecialtyResponse } from '../../../../models/master-data.model';

// PrimeNG Modules
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { InputTextareaModule } from 'primeng/inputtextarea'; // Thêm cái này cho mô tả
import { MessageService, ConfirmationService } from 'primeng/api';

@Component({
  selector: 'app-specialty-management',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, FormsModule,
    TableModule, ButtonModule, InputTextModule, InputTextareaModule,
    DialogModule, ToastModule, ConfirmDialogModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './specialty-management.component.html',
  styleUrls: ['./specialty-management.component.scss']
})
export class SpecialtyManagementComponent implements OnInit {
  // Data
  specialties: SpecialtyResponse[] = [];
  totalRecords: number = 0;
  loading: boolean = false;

  // Pagination Params
  page: number = 1;
  size: number = 10;
  keyword: string = '';

  // Dialog & Form
  specialtyDialog: boolean = false;
  specialtyForm: FormGroup;
  submitted: boolean = false;
  isEditMode: boolean = false;

  // DI
  private masterDataService = inject(MasterDataService);
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);
  private fb = inject(FormBuilder);

  constructor() {
    this.specialtyForm = this.fb.group({
      specialtyId: [null],
      name: ['', [Validators.required]],
      description: ['']
    });
  }

  ngOnInit() {
    this.loadSpecialties();
  }

  loadSpecialties(event?: any) {
    this.loading = true;
    if (event) {
      this.page = (event.first / event.rows) + 1;
      this.size = event.rows;
    }

    // Gọi API lấy danh sách chuyên khoa
    this.masterDataService.getAllSpecialties(this.page, this.size, this.keyword).subscribe({
      next: (res) => {
        this.specialties = res.result?.data || [];
        this.totalRecords = res.result?.totalElements || 0;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  openNew() {
    this.isEditMode = false;
    this.specialtyForm.reset();
    this.submitted = false;
    this.specialtyDialog = true;
  }

  editSpecialty(item: SpecialtyResponse) {
    this.isEditMode = true;
    this.specialtyForm.patchValue({
      specialtyId: item.specialtyId,
      name: item.name,
      description: item.description
    });
    this.specialtyDialog = true;
  }

  saveSpecialty() {
    this.submitted = true;
    if (this.specialtyForm.invalid) return;

    const val = this.specialtyForm.value;

    if (this.isEditMode) {
      this.masterDataService.updateSpecialty(val.specialtyId, {
        name: val.name,
        description: val.description
      }).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật chuyên khoa' });
          this.specialtyDialog = false;
          this.loadSpecialties();
        }
      });
    } else {
      this.masterDataService.createSpecialty({
        name: val.name,
        description: val.description
      }).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã thêm chuyên khoa mới' });
          this.specialtyDialog = false;
          this.loadSpecialties();
        }
      });
    }
  }

  deleteSpecialty(id: number) {
    this.confirmationService.confirm({
      message: 'Bạn có chắc chắn muốn xóa chuyên khoa này?<br>Lưu ý: Các bác sĩ thuộc chuyên khoa này sẽ bị ảnh hưởng.',
      header: 'Xác nhận xóa',
      icon: 'pi pi-exclamation-triangle',
      acceptButtonStyleClass: 'p-button-danger',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        this.masterDataService.deleteSpecialty(id).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa chuyên khoa' });
            this.loadSpecialties();
          },
          error: () => {
             this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể xóa (Có thể dữ liệu đang được sử dụng)' });
          }
        });
      }
    });
  }
}
