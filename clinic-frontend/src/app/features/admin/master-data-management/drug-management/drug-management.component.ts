import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { MasterDataService } from '../../../../core/services/master-data.service';
import { DrugResponse } from '../../../../models/master-data.model';

// PrimeNG Modules
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { InputNumberModule } from 'primeng/inputnumber';
import { ConfirmationService, MessageService } from 'primeng/api';

@Component({
  selector: 'app-drug-management',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, FormsModule,
    TableModule, ButtonModule, InputTextModule,
    DialogModule, ToastModule, ConfirmDialogModule,
    InputTextareaModule, InputNumberModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './drug-management.component.html',
  styleUrls: ['./drug-management.component.scss']
})
export class DrugManagementComponent implements OnInit {
  // Data
  drugs: DrugResponse[] = [];
  totalRecords: number = 0;
  loading: boolean = false;

  // Pagination Params
  page: number = 1;
  size: number = 10;
  keyword: string = '';

  // Dialog & Form
  drugDialog: boolean = false;
  drugForm: FormGroup;
  submitted: boolean = false;
  isEditMode: boolean = false;

  private masterDataService = inject(MasterDataService);
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);
  private fb = inject(FormBuilder);

  constructor() {
    this.drugForm = this.fb.group({
      drugId: [null],
      name: ['', [Validators.required]],
      unit: ['', [Validators.required]],
      stockQuantity: [0, [Validators.required, Validators.min(0)]],
      price: [0, [Validators.required, Validators.min(0)]],
      instructions: ['']
    });
  }

  ngOnInit() {
    this.loadDrugs();
  }

  // 1. Load danh sách (Server-side pagination)
  loadDrugs(event?: any) {
    this.loading = true;

    // Logic tính trang dựa trên PrimeNG Table event
    if (event) {
      this.page = (event.first / event.rows) + 1;
      this.size = event.rows;
    }

    // Gọi API (Đã bỏ activeOnly theo yêu cầu của bạn)
    this.masterDataService.getDrugs(this.page, this.size, this.keyword).subscribe({
        next: (res) => {
            this.drugs = res.result?.data || [];
            this.totalRecords = res.result?.totalElements || 0;
            this.loading = false;
        },
        error: () => this.loading = false
    });
  }

  // 2. Mở Dialog Thêm mới
  openNew() {
    this.isEditMode = false;
    this.drugForm.reset();

    // Set giá trị mặc định cho số
    this.drugForm.patchValue({
        stockQuantity: 0,
        price: 0
    });

    this.submitted = false;
    this.drugDialog = true;
  }

  // 3. Mở Dialog Sửa
  editDrug(drug: DrugResponse) {
    this.isEditMode = true;
    this.drugForm.patchValue({ ...drug });
    this.drugDialog = true;
  }

  // 4. Lưu (Tạo mới hoặc Cập nhật)
  saveDrug() {
    this.submitted = true;
    if (this.drugForm.invalid) return;

    const formValue = this.drugForm.value;

    if (this.isEditMode) {
      // --- UPDATE ---
      const updateData = {
        name: formValue.name,
        unit: formValue.unit,
        instructions: formValue.instructions,
        stockQuantity: formValue.stockQuantity,
        price: formValue.price
      };

      this.masterDataService.updateDrug(formValue.drugId, updateData).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật thông tin thuốc' });
          this.hideDialog();
          this.loadDrugs();
        },
        error: (err) => this.showError(err)
      });

    } else {
      // --- CREATE ---
      const createData = {
        name: formValue.name,
        unit: formValue.unit,
        instructions: formValue.instructions,
        stockQuantity: formValue.stockQuantity,
        price: formValue.price
      };

      this.masterDataService.createDrug(createData).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã thêm thuốc mới' });
          this.hideDialog();
          this.loadDrugs();
        },
        error: (err) => this.showError(err)
      });
    }
  }

  // 5. Xóa thuốc
  deleteDrug(drug: DrugResponse) {
    this.confirmationService.confirm({
      message: `Bạn có chắc chắn muốn xóa thuốc <b>${drug.name}</b>?<br>Hành động này sẽ ẩn thuốc khỏi danh sách.`,
      header: 'Xác nhận xóa',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Xóa',
      rejectLabel: 'Hủy',
      acceptButtonStyleClass: 'p-button-danger',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        this.masterDataService.deleteDrug(drug.drugId).subscribe({
          next: () => {
            this.messageService.add({
              severity: 'success',
              summary: 'Thành công',
              detail: 'Đã xóa thuốc thành công'
            });
            this.loadDrugs();
          },
          error: (err) => this.showError(err)
        });
      }
    });
  }

  hideDialog() {
    this.drugDialog = false;
    this.submitted = false;
  }

  showError(err: any) {
    const msg = err.error?.message || 'Có lỗi xảy ra!';
    this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: msg });
  }

  exportExcel() {
    this.loading = true; // Bật loading table trong lúc chờ tải

    this.masterDataService.exportDrugs().subscribe({
      next: (blob: Blob) => {
        // Tạo thẻ a ảo để kích hoạt download
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;

        // Đặt tên file tải về
        const timestamp = new Date().toISOString().slice(0, 10);
        a.download = `Danh_sach_thuoc_${timestamp}.xlsx`;

        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        window.URL.revokeObjectURL(url);

        this.loading = false;
        this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xuất file Excel' });
      },
      error: (err) => {
        this.loading = false;
        this.showError(err);
      }
    });
  }
}
