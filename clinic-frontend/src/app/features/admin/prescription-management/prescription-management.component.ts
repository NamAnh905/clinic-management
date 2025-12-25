import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule, formatDate } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { MedicalService } from '../../../core/services/medical.service';
import { MasterDataService } from '../../../core/services/master-data.service';
import { BillingService } from '../../../core/services/billing.service';
import { MedicalRecordResponse, PresDetailResponse, PrescriptionResponse } from '../../../models/medical.model';
import { MessageService, ConfirmationService } from 'primeng/api';
import { AuthService } from '../../../core/services/auth.service';

// PrimeNG Modules
import { TableModule, Table } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { CalendarModule } from 'primeng/calendar';
import { DialogModule } from 'primeng/dialog';
import { TooltipModule } from 'primeng/tooltip';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DropdownModule } from 'primeng/dropdown';

@Component({
  selector: 'app-prescription',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule,
    TableModule, ButtonModule, InputTextModule, CalendarModule,
    DialogModule, TooltipModule, ToastModule, ConfirmDialogModule, DropdownModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './prescription-management.component.html',
  styleUrls: ['./prescription-management.component.scss']
})
export class PrescriptionComponent implements OnInit {
  @ViewChild('dt') dt: Table | undefined;

  // --- MAIN TABLE DATA (Medical Records) ---
  records: MedicalRecordResponse[] = [];
  totalRecords: number = 0;
  loading: boolean = false;

  // --- FILTERS ---
  keyword: string = '';
  rangeDates: Date[] | undefined;
  page: number = 1;
  size: number = 10;
  typingTimer: any;

  // --- DETAIL DIALOG LOGIC ---
  presDialog: boolean = false;
  currentRecord: MedicalRecordResponse | null = null;

  // Đơn thuốc & Chi tiết
  hasPrescription: boolean = false;
  currentPrescription: PrescriptionResponse | null = null;
  currentDetails: PresDetailResponse[] = [];
  loadingDetails: boolean = false;

  currentInvoiceStatus: string | null = null;

  // --- FORM THUỐC ---
  drugForm: FormGroup;
  drugsList: any[] = [];

  today: Date = new Date();

  // Inject Services
  private medicalService = inject(MedicalService);
  private messageService = inject(MessageService);
  private masterDataService = inject(MasterDataService);
  private billingService = inject(BillingService);
  private confirmationService = inject(ConfirmationService);
  private fb = inject(FormBuilder);
  public authService = inject(AuthService)

  constructor() {
    this.drugForm = this.fb.group({
      drugId: [null, Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
      dosage: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.loadMasterDrugs();
  }

  // --- 1. LOAD DATA CHO BẢNG CHÍNH ---
  loadRecords(event?: any) {
    this.loading = true;
    if (event) {
      this.page = (event.first / event.rows) + 1;
      this.size = event.rows;
    }

    let fromDateStr = '';
    let toDateStr = '';
    // Format ngày gửi xuống BE: yyyy-MM-ddTHH:mm:ss
    if (this.rangeDates && this.rangeDates[0]) {
      fromDateStr = formatDate(this.rangeDates[0], 'yyyy-MM-dd', 'en-US') + 'T00:00:00';
    }
    if (this.rangeDates && this.rangeDates[1]) {
      toDateStr = formatDate(this.rangeDates[1], 'yyyy-MM-dd', 'en-US') + 'T23:59:59';
    }

    this.medicalService.getMedicalRecords(this.page, this.size, this.keyword, fromDateStr, toDateStr)
      .subscribe({
        next: (res) => {
          this.records = res.result?.data || [];
          this.totalRecords = res.result?.totalElements || 0;
          this.loading = false;
        },
        error: () => {
            this.loading = false;
            this.records = [];
        }
      });
  }

  onGlobalFilter(event: any) {
    clearTimeout(this.typingTimer);
    this.typingTimer = setTimeout(() => {
      this.keyword = event.target.value;
      if (this.dt) this.dt.reset();
    }, 500);
  }


  printPrescription() {
      this.today = new Date();

      setTimeout(() => {
          window.print();
      }, 100);
  }
  // --- 2. LOGIC DIALOG ĐƠN THUỐC ---

  // Bước 1: Mở Dialog & Check đơn thuốc tồn tại
  openPrescription(record: MedicalRecordResponse) {
    this.currentRecord = record;
    this.presDialog = true;

    // Reset state
    this.hasPrescription = false;
    this.currentPrescription = null;
    this.currentDetails = [];
    this.drugForm.reset({ quantity: 1 });
    this.loadingDetails = true;
    this.currentInvoiceStatus = null; // Reset

    // 1. CHECK HÓA ĐƠN
    this.billingService.getInvoiceByAppointment(record.appointmentId).subscribe({
        next: (res) => {
            this.currentInvoiceStatus = res.result?.paymentStatus || null;
        },
        error: () => {
            this.currentInvoiceStatus = null;
        }
    });

    // 2. CHECK ĐƠN THUỐC
    this.medicalService.getPrescriptionByRecord(record.recordId).subscribe({
      next: (res) => {
        if (res.result) {
          this.hasPrescription = true;
          this.currentPrescription = res.result;
          this.loadPrescriptionDetails(res.result.prescriptionId);
        } else {
          this.hasPrescription = false;
          this.loadingDetails = false;
        }
      },
      error: () => {
        this.hasPrescription = false;
        this.loadingDetails = false;
      }
    });
  }

  // Bước 2: Load danh sách thuốc trong đơn
  loadPrescriptionDetails(presId: number) {
      this.medicalService.getPrescriptionDetails(presId).subscribe({
          next: (res) => {
              this.currentDetails = res.result || [];
              this.loadingDetails = false;
          },
          error: () => this.loadingDetails = false
      });
  }

  // Bước 3: Tạo vỏ đơn thuốc mới (Header)
  createPrescription() {
    if (!this.currentRecord) return;

    const request = {
      recordId: this.currentRecord.recordId,
      note: `Đơn thuốc ngày ${formatDate(new Date(), 'dd/MM/yyyy', 'en-US')}`
    };

    this.medicalService.createPrescription(request).subscribe({
      next: (res) => {
        this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã tạo đơn thuốc mới' });
        this.hasPrescription = true;
        this.currentPrescription = res.result || null;
      },
      error: (err) => this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tạo đơn thuốc' })
    });
  }

  // Bước 4: Thêm thuốc (Detail)
  addDrug() {
    if (this.drugForm.invalid || !this.currentPrescription) return;

    const val = this.drugForm.value;
    const request = {
      prescriptionId: this.currentPrescription.prescriptionId,
      drugId: val.drugId,
      quantity: val.quantity,
      dosage: val.dosage
    };

    this.medicalService.addDrugToPrescription(request).subscribe({
      next: (res) => {
        this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã thêm thuốc' });
        // Load lại danh sách chi tiết
        if (this.currentPrescription) {
            this.loadPrescriptionDetails(this.currentPrescription.prescriptionId);
        }
        // Reset form giữ lại quantity = 1 cho tiện
        this.drugForm.reset({ quantity: 1 });
      },
      error: (err) => this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: err.error?.message || 'Lỗi thêm thuốc' })
    });
  }

  // Bước 5: Xóa thuốc
  removeDrug(detailId: number) {
    this.confirmationService.confirm({
      message: 'Bạn chắc chắn muốn xóa thuốc này khỏi đơn?',
      header: 'Xác nhận xóa',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Xóa',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        this.medicalService.removeDrugFromPrescription(detailId).subscribe({
          next: () => {
             this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa thuốc khỏi đơn' });
             if (this.currentPrescription) {
                 this.loadPrescriptionDetails(this.currentPrescription.prescriptionId);
             }
          },
          error: () => this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể xóa thuốc' })
        });
      }
    });
  }

  // --- HELPERS ---
  loadMasterDrugs() {
      this.masterDataService.getDrugs(1, 1000).subscribe({
          next: (res) => {
              const list = res.result?.data || [];

              this.drugsList = list.map((d: any) => ({
                  label: `${d.name} (${d.unit})`,
                  value: d.drugId,
                  unit: d.unit,
                  price: d.price
              }));
          },
          error: (err) => {
              console.error('Lỗi tải danh sách thuốc', err);
              this.drugsList = [];
          }
      });
  }
}
