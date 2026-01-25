import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, formatDate } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { AppointmentService } from '../../../api/appointment.service';
import { AppointmentResponse } from '../../../models/appointment.model';
import { AppointmentStatus } from '../../../models/core.model';
import { UserService } from '../../../api/user.service';
import { BillingService } from '../../../api/billing.service';
import { AuthService } from '../../../api/auth.service';
import { InvoiceResponse, InvoiceDetailResponse, InvoiceDetailCreationRequest } from '../../../models/billing.model';
import { MasterDataService } from '../../../api/master-data.service'; // [NEW] Import MasterData

// PrimeNG Modules
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { DropdownModule } from 'primeng/dropdown';
import { CalendarModule } from 'primeng/calendar';
import { TagModule } from 'primeng/tag';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { ConfirmationService, MessageService } from 'primeng/api';
import { RadioButtonModule } from 'primeng/radiobutton';
import { InputNumberModule } from 'primeng/inputnumber';

@Component({
  selector: 'app-appointment-management',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, FormsModule,
    TableModule, ButtonModule, InputTextModule,
    DialogModule, ToastModule, ConfirmDialogModule,
    DropdownModule, CalendarModule, TagModule, InputTextareaModule,
    RadioButtonModule, InputNumberModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './appointment-management.component.html',
  styleUrls: ['./appointment-management.component.scss']
})
export class AppointmentManagementComponent implements OnInit {
  // Data
  appointments: AppointmentResponse[] = [];
  totalRecords: number = 0;
  loading: boolean = false;

  // Filters
  page: number = 1;
  size: number = 10;
  keyword: string = '';
  selectedStatus: AppointmentStatus | null = null;
  rangeDates: Date[] | undefined;

  statusOptions = [
    { label: 'Tất cả trạng thái', value: null },
    { label: 'Chờ xác nhận', value: 'PENDING' },
    { label: 'Đã xác nhận', value: 'CONFIRMED' },
    { label: 'Đã check-in', value: 'CHECKED_IN' },
    { label: 'Đang khám', value: 'IN_PROGRESS' },
    { label: 'Đã hoàn thành', value: 'COMPLETED' },
    { label: 'Đã hủy', value: 'CANCELLED' },
    { label: 'Vắng mặt (No-show)', value: 'NO_SHOW' }
  ];

  doctors: any[] = [];
  patients: any[] = [];

  apptDialog: boolean = false;
  apptForm: FormGroup;
  submitted: boolean = false;
  isEditMode: boolean = false;

  // --- INVOICE VARIABLES ---
  invoiceDialog: boolean = false;
  currentInvoice: InvoiceResponse | null = null;
  invoiceDetails: InvoiceDetailResponse[] = [];
  loadingDetails: boolean = false;

  // --- ADD ITEM VARIABLES (MỚI - Để thêm thuốc/dịch vụ) ---
  addItemType: 'SERVICE' | 'DRUG' = 'SERVICE';
  servicesList: any[] = [];
  drugsList: any[] = [];
  selectedItemId: number | null = null;
  addItemQuantity: number = 1;

  currentUser: any = null;

  // DI Services
  private appointmentService = inject(AppointmentService);
  private userService = inject(UserService);
  private billingService = inject(BillingService);
  private masterDataService = inject(MasterDataService); // [NEW]
  public authService = inject(AuthService);
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);
  private fb = inject(FormBuilder);

  // Getter tiện lợi
  get isDoctor(): boolean { return this.authService.isDoctor; }
  get isAdmin(): boolean { return this.authService.isAdmin; }

  constructor() {
    this.apptForm = this.fb.group({
      appointmentId: [null],
      patientId: [null, Validators.required],
      doctorId: [null, Validators.required],
      date: [null, Validators.required],
      time: [null, Validators.required],
      reason: ['', Validators.required],
      status: ['PENDING']
    });
  }

  ngOnInit() {
    this.authService.currentUser$.subscribe(user => {
      this.currentUser = user;
    });

    this.loadAppointments();
    this.loadDoctorsAndPatients();
    this.loadMasterData(); // [NEW] Tải danh sách thuốc/dịch vụ
  }

  // --- 1. LOGIC LOAD DỮ LIỆU ---
  loadMasterData() {
      // Load Dịch vụ
      this.masterDataService.getAllServices(1, 1000).subscribe({
          next: (res) => {
              this.servicesList = res.result?.data.map((s: any) => ({
                  label: `${s.name} (${this.formatCurrency(s.price)})`,
                  value: s.serviceId,
                  price: s.price
              })) || [];
          }
      });

      // Load Thuốc
      this.masterDataService.getDrugs(1, 1000).subscribe({
          next: (res) => {
              this.drugsList = res.result?.data.map((d: any) => ({
                  label: `${d.name} - ${d.unit} (${this.formatCurrency(d.price)})`,
                  value: d.drugId,
                  price: d.price
              })) || [];
          }
      });
  }

  loadAppointments(event?: any) {
      this.loading = true;

      if (event) {
        this.page = (event.first / event.rows) + 1;
        this.size = event.rows;
      }

      let sortBy = 'appointmentTime';
      let sortDir = 'desc';

      if (event && event.sortField) {
          sortBy = event.sortField;
          sortDir = event.sortOrder === 1 ? 'asc' : 'desc';
      }

      let fromDateStr = '';
      let toDateStr = '';
      if (this.rangeDates && this.rangeDates[0]) {
        fromDateStr = formatDate(this.rangeDates[0], 'yyyy-MM-dd', 'en-US') + 'T00:00:00';
      }
      if (this.rangeDates && this.rangeDates[1]) {
        toDateStr = formatDate(this.rangeDates[1], 'yyyy-MM-dd', 'en-US') + 'T23:59:59';
      }

      const myDoctorId = this.isDoctor ? (this.authService.currentDoctorIdSubject.value || undefined) : undefined;

      this.appointmentService.getAppointments(
        this.page, this.size, myDoctorId, undefined, this.keyword,
        this.selectedStatus || undefined, fromDateStr || undefined, toDateStr || undefined,
        sortBy, sortDir
      ).subscribe({
          next: (res) => {
              this.appointments = res.result?.data || [];
              this.totalRecords = res.result?.totalElements || 0;
              this.loading = false;
          },
          error: () => this.loading = false
      });
  }

  loadDoctorsAndPatients() {
    this.userService.getUsers(1, 100, true, 'DOCTOR').subscribe(res => {
       this.doctors = res.result?.data.map((u: any) => ({ label: u.fullName, value: u.userId })) || [];
    });
    this.userService.getUsers(1, 100, true, 'PATIENT').subscribe(res => {
       this.patients = res.result?.data.map((u: any) => ({ label: `${u.fullName} (${u.phoneNumber || '---'})`, value: u.userId })) || [];
    });
  }

  // --- 2. LOGIC LỊCH HẸN (CRUD) ---
  openNew() {
    this.isEditMode = false;
    this.apptForm.reset({ status: 'PENDING' });
    this.submitted = false;
    this.apptDialog = true;
  }

  editAppointment(appt: AppointmentResponse) {
    this.isEditMode = true;
    const fullDate = new Date(appt.appointmentTime);
    this.appointmentService.getAppointmentDetail(appt.appointmentId).subscribe(res => {
        const detail = res.result;
        if(detail){
             this.apptForm.patchValue({
                appointmentId: detail.appointmentId,
                patientId: detail.patientId,
                doctorId: detail.doctorId,
                reason: detail.reason,
                status: detail.status,
                date: fullDate,
                time: fullDate
            });
            this.apptDialog = true;
        }
    });
  }

  saveAppointment() {
    this.submitted = true;
    if (this.apptForm.controls['doctorId'].disabled) this.apptForm.controls['doctorId'].enable();
    if (this.apptForm.invalid) return;

    const val = this.apptForm.value;
    if (this.isDoctor) this.apptForm.controls['doctorId'].disable();

    const datePart = formatDate(val.date, 'yyyy-MM-dd', 'en-US');
    const timePart = formatDate(val.time, 'HH:mm:00', 'en-US');
    const combinedDateTime = `${datePart}T${timePart}`;

    if (this.isEditMode) {
        const updateData = { appointmentTime: combinedDateTime, reason: val.reason, status: val.status };
        this.appointmentService.updateAppointment(val.appointmentId, updateData).subscribe({
            next: () => {
                this.messageService.add({severity:'success', summary:'Thành công', detail:'Đã cập nhật lịch hẹn'});
                this.hideDialog();
                this.loadAppointments();
            },
            error: (err) => this.showError(err)
        });
    } else {
        const createData = { patientId: val.patientId, doctorId: val.doctorId, appointmentTime: combinedDateTime, reason: val.reason };
        this.appointmentService.bookAppointment(createData).subscribe({
            next: () => {
                this.messageService.add({severity:'success', summary:'Thành công', detail:'Đã đặt lịch hẹn mới'});
                this.hideDialog();
                this.loadAppointments();
            },
            error: (err) => this.showError(err)
        });
    }
  }

  cancelAppointment(appt: AppointmentResponse) {
    this.confirmationService.confirm({
        message: `Bạn muốn hủy lịch hẹn của <b>${appt.patientName}</b>?`,
        header: 'Xác nhận hủy',
        icon: 'pi pi-exclamation-triangle',
        acceptLabel: 'Đồng ý hủy',
        rejectLabel: 'Quay lại',
        acceptButtonStyleClass: 'p-button-danger',
        accept: () => {
            const updateData = { status: 'CANCELLED' as AppointmentStatus };
            this.appointmentService.updateAppointment(appt.appointmentId, updateData).subscribe({
                next: () => {
                    this.messageService.add({severity:'success', summary:'Đã hủy', detail:'Lịch hẹn đã được hủy bỏ'});
                    this.loadAppointments();
                },
                error: (err) => this.showError(err)
            });
        }
    });
  }

  exportExcel() {
    this.loading = true;
    this.appointmentService.exportAppointments().subscribe({
        next: (blob) => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            const timestamp = formatDate(new Date(), 'dd-MM-yyyy', 'en-US');
            a.download = `Danh_sach_lich_hen_${timestamp}.xlsx`;
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

  // --- 3. LOGIC HÓA ĐƠN & THANH TOÁN (QUAN TRỌNG) ---

  handlePayment(appt: AppointmentResponse) {
    if (appt.status !== 'COMPLETED') {
        this.messageService.add({ severity: 'warn', summary: 'Chưa hoàn thành', detail: 'Chỉ có thể tạo hóa đơn cho cuộc hẹn đã hoàn thành.' });
        return;
    }
    this.loading = true;

    this.billingService.getInvoiceByAppointment(appt.appointmentId).subscribe({
        next: (res) => {
            const invoice = res.result;

            if (invoice && invoice.type === 'BOOKING') {
                // Nếu chỉ có BOOKING -> Tạo mới FINAL
                this.createAutoInvoice(appt.appointmentId);
            }
            else if (invoice && invoice.type === 'FINAL') {
                this.loading = false;
                this.openInvoiceDialog(invoice);
            }
            else {
                this.createAutoInvoice(appt.appointmentId);
            }
        },
        error: (err) => {
            if (err.status === 404 || err.status === 400 || err.error?.code === 1008) {
                 this.createAutoInvoice(appt.appointmentId);
            } else {
                this.loading = false;
                this.showError(err);
            }
        }
    });
  }

  createAutoInvoice(appointmentId: number) {
      const request = { appointmentId: appointmentId, paymentMethod: 'CASH', paymentStatus: 'PENDING' };
      this.billingService.createInvoice(request as any).subscribe({
          next: (res) => {
              this.loading = false;
              this.messageService.add({ severity: 'success', summary: 'Đã tạo hóa đơn', detail: 'Hóa đơn tất toán đã được tạo.' });
              if (res.result) this.openInvoiceDialog(res.result);
          },
          error: (err) => {
              this.loading = false;
              this.showError(err);
          }
      });
  }

  openInvoiceDialog(invoice: InvoiceResponse) {
      // Gọi API lấy thông tin mới nhất (để cập nhật TotalAmount chính xác)
      this.billingService.getInvoiceById(invoice.invoiceId).subscribe(res => {
          this.currentInvoice = res.result;
          this.invoiceDialog = true;
          this.loadingDetails = true;
          this.invoiceDetails = [];

          // Reset form thêm item
          this.selectedItemId = null;
          this.addItemQuantity = 1;

          this.billingService.getDetailsByInvoice(invoice.invoiceId).subscribe({
              next: (detailRes) => {
                  let realDetails = detailRes.result || [];

                  // --- [LOGIC MỚI] DÒNG ẢO HIỂN THỊ CỌC ---
                  // Nếu là Hóa đơn FINAL và chưa thanh toán
                  if (this.currentInvoice?.type === 'FINAL' && this.currentInvoice.paymentStatus === 'PENDING') {

                      // Nếu danh sách chi tiết RỖNG (nghĩa là backend không tự cộng tiền khám do đã trả cọc)
                      // Hoặc bạn có thể check chính xác hơn nếu muốn
                      if (realDetails.length === 0) {
                          const consultationPrice = 200000; // Giá khám (Cấu hình hoặc lấy từ DB)

                          const depositLine = {
                              detailId: -1, // ID âm để nhận biết là dòng ảo
                              serviceName: 'Phí đặt lịch (Đã thanh toán)',
                              quantity: 1,
                              unitPrice: consultationPrice,
                              drugName: null
                          } as any;

                          const deductionLine = {
                              detailId: -2,
                              serviceName: 'Khấu trừ cọc',
                              quantity: 1,
                              unitPrice: -consultationPrice, // Giá âm
                              drugName: null
                          } as any;

                          // Chèn vào đầu danh sách
                          realDetails = [depositLine, deductionLine, ...realDetails];
                      }
                  }
                  // ----------------------------------------

                  this.invoiceDetails = realDetails;
                  this.loadingDetails = false;
              },
              error: () => this.loadingDetails = false
          });
      });
  }

  // --- 4. LOGIC THÊM/XÓA ITEM VÀO HÓA ĐƠN ---
  addItemToInvoice() {
      if (!this.currentInvoice || !this.selectedItemId) return;

      const request: InvoiceDetailCreationRequest = {
          invoiceId: this.currentInvoice.invoiceId,
          quantity: this.addItemQuantity,
          serviceId: this.addItemType === 'SERVICE' ? this.selectedItemId : undefined,
          drugId: this.addItemType === 'DRUG' ? this.selectedItemId : undefined
      };

      this.billingService.addInvoiceDetail(request).subscribe({
          next: () => {
              this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã thêm vào hóa đơn' });
              this.selectedItemId = null;
              this.addItemQuantity = 1;
              if (this.currentInvoice) this.openInvoiceDialog(this.currentInvoice);
          },
          error: (err) => this.showError(err)
      });
  }

  deleteInvoiceDetail(detailId: number) {
      // Chặn xóa dòng ảo
      if (detailId < 0) {
          this.messageService.add({severity: 'warn', summary: 'Không thể xóa', detail: 'Đây là dòng thông tin cọc, không thể xóa.'});
          return;
      }

      this.confirmationService.confirm({
          message: 'Xóa mục này khỏi hóa đơn?',
          header: 'Xác nhận xóa',
          icon: 'pi pi-trash',
          acceptLabel: 'Xóa',
          acceptButtonStyleClass: 'p-button-danger',
          rejectLabel: 'Hủy',
          accept: () => {
              this.billingService.deleteInvoiceDetail(detailId).subscribe({
                  next: () => {
                      this.messageService.add({ severity: 'success', summary: 'Đã xóa', detail: 'Đã cập nhật hóa đơn' });
                      if (this.currentInvoice) this.openInvoiceDialog(this.currentInvoice);
                  },
                  error: (err) => this.showError(err)
              });
          }
      });
  }

  confirmPayment() {
      if (!this.currentInvoice) return;

      this.confirmationService.confirm({
        message: `Xác nhận thu tiền mặt cho hóa đơn <b>#${this.currentInvoice.transactionCode || this.currentInvoice.invoiceId}</b>?<br>Tổng tiền: <b class="text-primary">${this.formatCurrency(this.currentInvoice.totalAmount)}</b>`,
        header: 'Xác nhận thu tiền',
        icon: 'pi pi-wallet',
        acceptLabel: 'Thanh toán',
        rejectLabel: 'Hủy',
        acceptButtonStyleClass: 'p-button-success',
        accept: () => {
          const request: any = {
            paymentStatus: 'PAID',
            paymentMethod: 'CASH'
          };

          this.billingService.updateInvoice(this.currentInvoice!.invoiceId, request).subscribe({
            next: () => {
              this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Thanh toán thành công!' });
              this.invoiceDialog = false;
              this.loadAppointments();
            },
            error: (err) => this.showError(err)
          });
        }
      });
  }

  payWithVnPay() {
      if (!this.currentInvoice) return;

      this.loadingDetails = true;
      this.billingService.initiateVnPayPayment(this.currentInvoice.invoiceId).subscribe({
          next: (res) => {
              if (res.result) {
                  window.location.href = res.result;
              } else {
                  this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không lấy được link thanh toán' });
              }
              this.loadingDetails = false;
          },
          error: (err) => {
              this.showError(err);
              this.loadingDetails = false;
          }
      });
  }

  // --- HELPERS ---
  hideDialog() { this.apptDialog = false; this.submitted = false; }

  showError(err: any) {
    const msg = err.error?.message || 'Có lỗi xảy ra!';
    this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: msg });
  }

  formatCurrency(amount: number): string {
      return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  }

  getItemName(item: InvoiceDetailResponse): string { return item.serviceName || item.drugName || 'Không xác định'; }

  getSeverity(status: string) {
    switch (status) {
        case 'CONFIRMED': return 'success';
        case 'PENDING': return 'warning';
        case 'COMPLETED': return 'info';
        case 'CHECKED_IN': return 'secondary';
        case 'IN_PROGRESS': return 'warning';
        case 'CANCELLED': return 'danger';
        case 'NO_SHOW': return 'danger';
        default: return 'contrast';
    }
  }

  getStatusClass(status: string): string {
      switch (status) {
          case 'PAID': return 'status-paid';
          case 'PENDING': return 'status-pending';
          case 'FAILED': return 'status-failed';
          default: return '';
      }
  }

  getStatusLabel(status: string): string {
      switch (status) {
          case 'PAID': return 'Đã thanh toán';
          case 'PENDING': return 'Chờ thanh toán';
          case 'FAILED': return 'Đã hủy';
          default: return status;
      }
  }
}
