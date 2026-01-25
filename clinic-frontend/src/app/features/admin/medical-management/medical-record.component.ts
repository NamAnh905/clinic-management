import { Component, OnInit, inject, ViewChild } from '@angular/core';
import { CommonModule, formatDate } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { MedicalService } from '../../../api/medical.service';
import { MasterDataService } from '../../../api/master-data.service';
import { BillingService } from '../../../api/billing.service';
import { MedicalRecordResponse, PrescriptionResponse, PresDetailResponse } from '../../../models/medical.model';
import { ConfirmationService, MessageService } from 'primeng/api';
import { AuthService } from '../../../api/auth.service';

// PrimeNG Modules
import { TableModule, Table } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { ToastModule } from 'primeng/toast';
import { TooltipModule } from 'primeng/tooltip';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DropdownModule } from 'primeng/dropdown';
import { CalendarModule } from 'primeng/calendar';

@Component({
  selector: 'app-medical-record',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, FormsModule,
    TableModule, ButtonModule, DialogModule, InputTextModule,
    InputTextareaModule, ToastModule, TooltipModule, ConfirmDialogModule,
    DropdownModule, CalendarModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './medical-record.component.html',
  styleUrls: ['./medical-record.component.scss']
})
export class MedicalRecordComponent implements OnInit {
  @ViewChild('dt') dt: Table | undefined;

  // --- 1. BIẾN QUẢN LÝ BỆNH ÁN (CŨ) ---
  records: MedicalRecordResponse[] = [];
  totalRecords: number = 0;
  loading: boolean = false;
  typingTimer: any;
  keyword: string = '';
  rangeDates: Date[] | undefined;
  page: number = 1;
  size: number = 10;

  recordDialog: boolean = false;
  isEditMode: boolean = false;
  recordForm: FormGroup;

  patients: any[] = [];
  eligibleAppointments: any[] = [];

  // --- 2. BIẾN QUẢN LÝ ĐƠN THUỐC (MỚI) ---

  // [FIX 1] Thêm biến record vào đây để sửa lỗi "Property record does not exist"
  record: MedicalRecordResponse | null = null;

  presDialog: boolean = false;
  hasPrescription: boolean = false;
  currentPrescription: PrescriptionResponse | null = null;
  currentDetails: PresDetailResponse[] = [];
  loadingDetails: boolean = false;
  selectedRecordId: number | null = null; // Lưu ID bệnh án đang thao tác

  drugForm: FormGroup;
  drugsList: any[] = []; // Danh sách thuốc dropdown

  currentInvoiceStatus: string | null = null;

  currentInvoiceType: string = '';
  isFinalPaid: boolean = false;

  // Inject Services
  private medicalService = inject(MedicalService);
  private masterDataService = inject(MasterDataService);
  public billingService = inject(BillingService); // Public để dùng trong HTML nếu cần
  private messageService = inject(MessageService);
  public authService = inject(AuthService);
  private confirmationService = inject(ConfirmationService);
  private fb = inject(FormBuilder);

  constructor() {
    // Form Bệnh án
    this.recordForm = this.fb.group({
      recordId: [null],
      appointmentId: [null, Validators.required],
      height: [null],
      weight: [null],
      bloodPressure: [''],
      temperature: [null],
      heartRate: [null],
      symptoms: ['', Validators.required],
      diagnosis: ['', Validators.required],
      treatmentPlan: ['']
    });

    // Form Thuốc (Mới)
    this.drugForm = this.fb.group({
      drugId: [null, Validators.required],
      quantity: [1, [Validators.required, Validators.min(1)]],
      dosage: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.loadPatients();
    this.loadMasterDrugs();
  }

  // --- LOGIC LOAD DỮ LIỆU ---

  loadPatients() {
    this.medicalService.getAllPatients().subscribe({
        next: (res) => {
            this.patients = res.result?.data.map((p: any) => ({
                label: `${p.patientName} (SĐT: ${p.phoneNumber})`,
                value: p.patientId
            })) || [];
        }
    });
  }

  loadMasterDrugs() {
    this.masterDataService.getDrugs(1, 1000).subscribe({
        next: (res) => {
            const list = res.result?.data || [];
            this.drugsList = list.map((d: any) => ({
                label: `${d.name} (${d.unit})`,
                value: d.drugId
            }));
        },
        error: () => this.drugsList = []
    });
  }

  loadMedicalHistory(event?: any) {
      this.loading = true;

      // 1. Xử lý Phân trang (Pagination)
      if (event) {
        this.page = (event.first / event.rows) + 1;
        this.size = event.rows;
      }

      // 2. Xử lý Sắp xếp (Sorting) - MỚI THÊM
      // Mặc định: Sắp theo ngày khám mới nhất
      let sortBy = 'visitDate';
      let sortDir = 'desc';

      if (event && event.sortField) {
        sortBy = event.sortField; // Lấy key từ pSortableColumn (vd: patientName)

        // PrimeNG: 1 là ASC, -1 là DESC. Cần đổi sang chuỗi cho Backend hiểu
        sortDir = event.sortOrder === 1 ? 'asc' : 'desc';
      }

      // 3. Xử lý Lọc ngày (Date Filter)
      let fromDateStr = '';
      let toDateStr = '';
      if (this.rangeDates && this.rangeDates[0]) {
        fromDateStr = formatDate(this.rangeDates[0], 'yyyy-MM-dd', 'en-US') + 'T00:00:00';
      }
      if (this.rangeDates && this.rangeDates[1]) {
        toDateStr = formatDate(this.rangeDates[1], 'yyyy-MM-dd', 'en-US') + 'T23:59:59';
      }

      // 4. Gọi Service với đầy đủ tham số
      this.medicalService.getMedicalRecords(
          this.page,
          this.size,
          this.keyword,
          fromDateStr,
          toDateStr,
          undefined,
          sortBy,   // Truyền tham số sort mới
          sortDir   // Truyền hướng sort
      ).subscribe({
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

  onPatientChange(event: any) {
      const patientId = event.value;
      this.eligibleAppointments = [];
      this.recordForm.patchValue({ appointmentId: null });

      if (patientId) {
          this.medicalService.getEligibleAppointments(patientId).subscribe({
              next: (res) => {
                  const appointments = res.result?.data || [];
                  this.eligibleAppointments = appointments.map((appt: any) => ({
                      label: `${formatDate(appt.appointmentTime, 'dd/MM/yyyy HH:mm', 'en-US')} - ${appt.reason || 'Không lý do'}`,
                      value: appt.appointmentId
                  }));
                  if (this.eligibleAppointments.length === 0) {
                      this.messageService.add({severity:'warn', summary:'Thông báo', detail:'Bệnh nhân này chưa có lịch hẹn nào đã xác nhận.'});
                  }
              }
          });
      }
  }

  // --- LOGIC BỆNH ÁN (CRUD) ---

  openNew() {
    this.isEditMode = false;
    this.recordForm.reset();
    this.recordDialog = true;
  }

  editRecord(record: MedicalRecordResponse) {
    this.isEditMode = true;
    this.recordForm.patchValue(record);
    this.recordDialog = true;
  }

  saveRecord() {
    if (this.recordForm.invalid) return;
    const val = this.recordForm.value;

    if (this.isEditMode) {
      this.medicalService.updateRecord(val.recordId, val).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã cập nhật hồ sơ' });
          this.recordDialog = false;
          this.loadMedicalHistory();
        },
        error: (err) => this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: err.error?.message })
      });
    } else {
      this.medicalService.createRecord(val).subscribe({
        next: () => {
          this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã tạo hồ sơ mới' });
          this.recordDialog = false;
          if (this.dt) this.dt.reset();
          else this.loadMedicalHistory();
        },
        error: (err) => this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: err.error?.message })
      });
    }
  }

  deleteRecord(record: MedicalRecordResponse) {
    this.confirmationService.confirm({
      message: `Bạn muốn xóa hồ sơ của <b>${record.patientName}</b>?`,
      header: 'Xác nhận xóa',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Xóa',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        this.medicalService.deleteRecord(record.recordId).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa hồ sơ' });
            this.loadMedicalHistory();
          },
          error: (err) => this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: err.error?.message })
        });
      }
    });
  }

  // --- LOGIC ĐƠN THUỐC (MỚI) ---

  openPrescriptionDialog(record: MedicalRecordResponse) {
      this.record = record; // [FIX 1] Đã có biến để gán
      this.selectedRecordId = record.recordId; // [FIX 2] Gán selectedRecordId để dùng cho hàm tạo đơn

      this.presDialog = true;
      this.currentDetails = [];
      this.hasPrescription = false;

      // Reset trạng thái cờ
      this.isFinalPaid = false;
      this.currentInvoiceStatus = '';

      const appointmentId = record.appointmentId;

      // 1. Kiểm tra xem đã có đơn thuốc chưa
      this.medicalService.getPrescriptionByRecord(record.recordId).subscribe({
          next: (res: any) => {
              const presData = res.result || res.data;

              if (presData) {
                  this.hasPrescription = true;
                  this.currentPrescription = presData;
                  this.loadPrescriptionDetails(presData.prescriptionId);
              } else {
                  this.hasPrescription = false;
                  this.currentPrescription = null;
              }
          },
          error: () => {
              this.hasPrescription = false;
          }
      });

      // 2. Kiểm tra trạng thái Hóa đơn
      this.billingService.getInvoiceByAppointment(appointmentId).subscribe({
          next: (res: any) => {
              const invoice = res.result || res.data;

              if (invoice) {
                  this.currentInvoiceStatus = invoice.paymentStatus;
                  this.currentInvoiceType = invoice.type; // Lưu type để debug nếu cần

                  // LOGIC QUAN TRỌNG: Chỉ chặn khi là FINAL và PAID
                  if (invoice.type === 'FINAL' && invoice.paymentStatus === 'PAID') {
                      this.isFinalPaid = true;
                  } else {
                      // BOOKING + PAID hoặc PENDING -> False (Cho phép sửa)
                      this.isFinalPaid = false;
                  }
              } else {
                  this.isFinalPaid = false;
              }
          },
          error: () => {
              this.isFinalPaid = false;
          }
      });
  }

  loadPrescriptionDetails(presId: number) {
    this.loadingDetails = true;
    this.medicalService.getPrescriptionDetails(presId).subscribe({
        next: (res) => {
            this.currentDetails = res.result || [];
            this.loadingDetails = false;
        },
        error: () => this.loadingDetails = false
    });
  }

  createPrescription() {
    if (!this.selectedRecordId) {
        this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không xác định được hồ sơ bệnh án' });
        return;
    }

    const request = {
      recordId: this.selectedRecordId,
      note: `Đơn thuốc tạo ngày ${formatDate(new Date(), 'dd/MM/yyyy', 'en-US')}`
    };

    this.medicalService.createPrescription(request).subscribe({
      next: (res) => {
        this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã tạo đơn thuốc mới' });
        this.hasPrescription = true;
        this.currentPrescription = res.result || null;
      },
      error: () => this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không thể tạo đơn thuốc' })
    });
  }

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
      next: () => {
        this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã thêm thuốc' });
        if (this.currentPrescription) this.loadPrescriptionDetails(this.currentPrescription.prescriptionId);
        this.drugForm.reset({ quantity: 1 });
      },
      error: (err) => this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: err.error?.message })
    });
  }

  removeDrug(detailId: number) {
    this.confirmationService.confirm({
      message: 'Xóa thuốc này khỏi đơn?',
      header: 'Xác nhận',
      icon: 'pi pi-exclamation-triangle',
      acceptLabel: 'Xóa',
      acceptButtonStyleClass: 'p-button-danger',
      accept: () => {
        this.medicalService.removeDrugFromPrescription(detailId).subscribe({
          next: () => {
             this.messageService.add({ severity: 'success', summary: 'Đã xóa', detail: 'Đã xóa thuốc khỏi đơn' });
             if (this.currentPrescription) this.loadPrescriptionDetails(this.currentPrescription.prescriptionId);
          }
        });
      }
    });
  }
}
