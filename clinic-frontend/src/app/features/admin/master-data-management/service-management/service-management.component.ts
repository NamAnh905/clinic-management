import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';

// PrimeNG
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DropdownModule } from 'primeng/dropdown';
import { InputNumberModule } from 'primeng/inputnumber'; // Dùng cho giá tiền
import { TagModule } from 'primeng/tag';
import { MessageService, ConfirmationService } from 'primeng/api';

// Services & Models
import { MasterDataService } from '../../../../core/services/master-data.service';
import { ServiceEntityResponse } from '../../../../models/master-data.model';

@Component({
  selector: 'app-service-management',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, FormsModule,
    TableModule, ButtonModule, InputTextModule, DialogModule,
    ToastModule, ConfirmDialogModule, DropdownModule, InputNumberModule, TagModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './service-management.component.html',
  styleUrls: ['./service-management.component.scss']
})
export class ServiceManagementComponent implements OnInit {
  services: ServiceEntityResponse[] = [];
  totalRecords: number = 0;
  loading: boolean = false;

  // Params query
  page: number = 1;
  size: number = 10;
  keyword: string = '';

  // Dialog & Form
  serviceDialog: boolean = false;
  serviceForm: FormGroup;
  submitted: boolean = false;
  isEditMode: boolean = false;
  currentServiceId: number | null = null;

  // Danh sách loại dịch vụ (Giả định enum ServiceType)
  serviceTypes = [
    { label: 'Khám bệnh', value: 'CONSULTATION' },
    { label: 'Khám cận lâm sàng', value: 'PARACLINICAL' },
    // { label: 'Xét nghiệm', value: 'TEST' },
    // { label: 'Chẩn đoán hình ảnh', value: 'IMAGING' },
    // { label: 'Phẫu thuật/Thủ thuật', value: 'SURGERY' }
  ];

  private masterDataService = inject(MasterDataService);
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);
  private fb = inject(FormBuilder);

  constructor() {
    this.serviceForm = this.fb.group({
      name: ['', Validators.required],
      price: [0, [Validators.required, Validators.min(1000)]],
      type: [null, Validators.required]
    });
  }

  ngOnInit() {
    this.loadServices();
  }

  loadServices(event?: any) {
    this.loading = true;
    if (event) {
      this.page = (event.first / event.rows) + 1;
      this.size = event.rows;
    }

    this.masterDataService.getAllServices(this.page, this.size, true, undefined, this.keyword).subscribe({
      next: (res) => {
        this.services = res.result?.data || [];
        this.totalRecords = res.result?.totalElements || 0;
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  openNew() {
    this.serviceForm.reset();
    this.isEditMode = false;
    this.submitted = false;
    this.serviceDialog = true;
  }

  editService(service: ServiceEntityResponse) {
    this.isEditMode = true;
    this.currentServiceId = service.serviceId;
    this.serviceForm.patchValue({
      name: service.name,
      price: service.price,
      type: service.type
    });
    this.submitted = false;
    this.serviceDialog = true;
  }

  saveService() {
    this.submitted = true;
    if (this.serviceForm.invalid) return;

    const val = this.serviceForm.value;

    if (this.isEditMode && this.currentServiceId) {
      // Update
      const payload = {
        name: val.name,
        price: val.price,
        type: val.type
      };

      this.masterDataService.updateService(this.currentServiceId, payload).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật dịch vụ' });
          this.serviceDialog = false;
          this.loadServices();
        },
        error: () => this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Cập nhật thất bại' })
      });
    } else {
      // Create
      this.masterDataService.createService(val).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã thêm dịch vụ mới' });
          this.serviceDialog = false;
          this.loadServices();
        },
        error: () => this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Thêm mới thất bại' })
      });
    }
  }

  deleteService(id: number) {
    this.confirmationService.confirm({
      message: 'Bạn có chắc chắn muốn xóa dịch vụ này không?',
      header: 'Xác nhận xóa',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Xóa',
      rejectLabel: 'Hủy',
      acceptButtonStyleClass: 'p-button-danger',
      rejectButtonStyleClass: 'p-button-text',
      accept: () => {
        this.masterDataService.deleteService(id).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa dịch vụ' });
            this.loadServices();
          },
          error: () => this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể xóa dịch vụ này' })
        });
      }
    });
  }

  // Helper để hiển thị loại dịch vụ đẹp hơn
  getServiceTypeLabel(type: string): string {
    const found = this.serviceTypes.find(t => t.value === type);
    return found ? found.label : type;
  }

  getSeverity(type: string): "success" | "info" | undefined {
    switch (type) {
      case 'CONSULTATION': return 'success';
      case 'PARACLINICAL': return 'info';
      default: return 'info';
    }
  }
}
