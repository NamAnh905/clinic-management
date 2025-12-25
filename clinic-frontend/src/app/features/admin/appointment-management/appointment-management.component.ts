import { Component, OnInit, inject } from '@angular/core';
import { CommonModule, formatDate } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormsModule } from '@angular/forms';
import { AppointmentService } from '../../../core/services/appointment.service';
import { AppointmentResponse } from '../../../models/appointment.model';
import { AppointmentStatus } from '../../../models/core.model';
import { UserService } from '../../../core/services/user.service';
import { BillingService } from '../../../core/services/billing.service';
import { AuthService } from '../../../core/services/auth.service';
import { InvoiceResponse, InvoiceDetailResponse } from '../../../models/billing.model';

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

@Component({
  selector: 'app-appointment-management',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, FormsModule,
    TableModule, ButtonModule, InputTextModule,
    DialogModule, ToastModule, ConfirmDialogModule,
    DropdownModule, CalendarModule, TagModule, InputTextareaModule
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

  // Dropdown Options
  statusOptions = [
    { label: 'Tất cả trạng thái', value: null },
    { label: 'Chờ xác nhận', value: 'PENDING' },
    { label: 'Đã xác nhận', value: 'CONFIRMED' },
    { label: 'Đã hoàn thành', value: 'COMPLETED' },
    { label: 'Đã hủy', value: 'CANCELLED' }
  ];

  doctors: any[] = [];
  patients: any[] = [];

  apptDialog: boolean = false;
  apptForm: FormGroup;
  submitted: boolean = false;
  isEditMode: boolean = false;

  invoiceDialog: boolean = false;
  currentInvoice: InvoiceResponse | null = null;
  invoiceDetails: InvoiceDetailResponse[] = [];
  loadingDetails: boolean = false;

  currentUser: any = null;

  // DI Services
  private appointmentService = inject(AppointmentService);
  private userService = inject(UserService);
  private billingService = inject(BillingService);
  public authService = inject(AuthService); // Public để dùng trong HTML nếu cần
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);
  private fb = inject(FormBuilder);

  // Getter tiện lợi dùng cho HTML (thay vì khai báo biến)
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
  }

  loadAppointments(event?: any) {
    this.loading = true;
    if (event) {
      this.page = (event.first / event.rows) + 1;
      this.size = event.rows;
    }

    let fromDateStr = '';
    let toDateStr = '';
    if (this.rangeDates && this.rangeDates[0]) {
      fromDateStr = formatDate(this.rangeDates[0], 'yyyy-MM-dd', 'en-US') + 'T00:00:00';
    }
    if (this.rangeDates && this.rangeDates[1]) {
      toDateStr = formatDate(this.rangeDates[1], 'yyyy-MM-dd', 'en-US') + 'T23:59:59';
    }

    // Thêm "|| undefined" để nếu là null thì biến thành undefined
    const myDoctorId = this.isDoctor ? (this.authService.currentDoctorIdSubject.value || undefined) : undefined;

    this.appointmentService.getAppointments(
      this.page,
      this.size,
      myDoctorId, // Tự động lọc theo bác sĩ hiện tại nếu là Doctor
      undefined,
      this.keyword,
      this.selectedStatus || undefined,
      fromDateStr || undefined,
      toDateStr || undefined
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
    // Nếu là Doctor, disable lại UI cho chắc
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

  hideDialog() { this.apptDialog = false; this.submitted = false; }

  showError(err: any) {
    const msg = err.error?.message || 'Có lỗi xảy ra!';
    this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: msg });
  }

  getSeverity(status: string) {
    switch (status) {
        case 'CONFIRMED': return 'success';
        case 'PENDING': return 'warning';
        case 'COMPLETED': return 'info';
        case 'CANCELLED': return 'danger';
        default: return 'secondary';
    }
  }

  handlePayment(appt: AppointmentResponse) {
    if (appt.status !== 'COMPLETED') {
        this.messageService.add({ severity: 'warn', summary: 'Chưa hoàn thành', detail: 'Chỉ có thể tạo hóa đơn cho cuộc hẹn đã hoàn thành.' });
        return;
    }
    this.loading = true;
    this.billingService.getInvoiceByAppointment(appt.appointmentId).subscribe({
        next: (res) => {
            this.loading = false;
            if (res.result) this.openInvoiceDialog(res.result);
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
              this.messageService.add({ severity: 'success', summary: 'Đã tạo hóa đơn', detail: 'Hóa đơn được tạo tự động.' });
              if (res.result) this.openInvoiceDialog(res.result);
          },
          error: (err) => {
              this.loading = false;
              this.showError(err);
          }
      });
  }

  openInvoiceDialog(invoice: InvoiceResponse) {
      this.currentInvoice = invoice;
      this.invoiceDialog = true;
      this.loadingDetails = true;
      this.invoiceDetails = [];
      this.billingService.getDetailsByInvoice(invoice.invoiceId).subscribe({
          next: (res) => {
              this.invoiceDetails = res.result || [];
              this.loadingDetails = false;
          },
          error: () => this.loadingDetails = false
      });
  }

  formatCurrency(amount: number): string {
      return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  }

  getItemName(item: InvoiceDetailResponse): string { return item.serviceName || item.drugName || 'Không xác định'; }

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
