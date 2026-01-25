import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';

// Services
import { MasterDataService } from '../../../../api/master-data.service';
import { UploadService } from '../../../../api/upload.service'; // <--- Import UploadService
import { SpecialtyResponse } from '../../../../models/master-data.model';

// PrimeNG Modules
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { MessageService, ConfirmationService } from 'primeng/api';
import { ImageModule } from 'primeng/image'; // <--- Thêm module Image để xem ảnh to

@Component({
  selector: 'app-specialty-management',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, FormsModule,
    TableModule, ButtonModule, InputTextModule, InputTextareaModule,
    DialogModule, ToastModule, ConfirmDialogModule, ImageModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './specialty-management.component.html',
  styleUrls: ['./specialty-management.component.scss']
})
export class SpecialtyManagementComponent implements OnInit {
  specialties: SpecialtyResponse[] = [];
  totalRecords: number = 0;
  loading: boolean = false;

  page: number = 1;
  size: number = 10;
  keyword: string = '';

  specialtyDialog: boolean = false;
  specialtyForm: FormGroup;
  submitted: boolean = false;
  isEditMode: boolean = false;

  // --- Image Logic ---
  uploadedImageUrl: string = '';
  isUploading: boolean = false;

  private masterDataService = inject(MasterDataService);
  private uploadService = inject(UploadService); // <--- Inject
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
    this.uploadedImageUrl = ''; // Reset ảnh
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
    this.uploadedImageUrl = item.image || ''; // Load ảnh cũ vào
    this.specialtyDialog = true;
  }

  // --- Xử lý upload ảnh ---
  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      this.isUploading = true;
      this.uploadService.uploadImage(file).subscribe({
        next: (res) => {
          if (res.result) {
            this.uploadedImageUrl = res.result; // Lưu URL trả về từ Cloudinary
            this.messageService.add({ severity: 'success', summary: 'Upload', detail: 'Tải ảnh thành công!' });
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

  saveSpecialty() {
    this.submitted = true;
    if (this.specialtyForm.invalid) return;

    const val = this.specialtyForm.value;

    // Tạo payload có kèm image
    const payload = {
        name: val.name,
        description: val.description,
        image: this.uploadedImageUrl // <--- Gửi kèm URL ảnh
    };

    if (this.isEditMode) {
      this.masterDataService.updateSpecialty(val.specialtyId, payload).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật chuyên khoa' });
          this.specialtyDialog = false;
          this.loadSpecialties();
        }
      });
    } else {
      this.masterDataService.createSpecialty(payload).subscribe({
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
