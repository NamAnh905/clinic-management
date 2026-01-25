import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule, formatDate } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { forkJoin, of } from 'rxjs';
import { BillingService } from '../../../api/billing.service';
import { MasterDataService } from '../../../api/master-data.service';
import { InvoiceResponse, InvoiceDetailResponse, InvoiceDetailCreationRequest } from '../../../models/billing.model';
import { PaymentStatus, PaymentMethod } from '../../../models/core.model';
import { MessageService, ConfirmationService } from 'primeng/api';

// PrimeNG Modules
import { TableModule, Table } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { CalendarModule } from 'primeng/calendar';
import { DialogModule } from 'primeng/dialog';
import { TooltipModule } from 'primeng/tooltip';
import { ToastModule } from 'primeng/toast';
import { TagModule } from 'primeng/tag';
import { DropdownModule } from 'primeng/dropdown';
import { ConfirmDialogModule } from 'primeng/confirmdialog';
import { RadioButtonModule } from 'primeng/radiobutton';
import { InputNumberModule } from 'primeng/inputnumber';

@Component({
  selector: 'app-invoice-management',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule,
    TableModule, ButtonModule, InputTextModule, CalendarModule,
    DialogModule, TooltipModule, ToastModule, TagModule, DropdownModule,
    ConfirmDialogModule, RadioButtonModule, InputNumberModule
  ],
  providers: [MessageService, ConfirmationService],
  templateUrl: './invoice-management.component.html',
  styleUrls: ['./invoice-management.component.scss']
})
export class InvoiceManagementComponent implements OnInit {
  @ViewChild('dt') dt: Table | undefined;

  // Data Tables
  invoices: InvoiceResponse[] = [];
  totalRecords: number = 0;
  loading: boolean = false;

  // Filters
  page: number = 1;
  size: number = 10;
  keyword: string = '';
  selectedStatus: PaymentStatus | null = null;
  rangeDates: Date[] | undefined;
  typingTimer: any;

  // Lọc Loại hóa đơn
  selectedType: string | null = null;
  typeOptions = [
      { label: 'Phí đặt lịch (Cọc)', value: 'BOOKING' },
      { label: 'Tất toán (Cuối)', value: 'FINAL' }
  ];

  statusOptions = [
    { label: 'Tất cả trạng thái', value: null },
    { label: 'Đã thanh toán', value: 'PAID' },
    { label: 'Chờ thanh toán', value: 'PENDING' },
    { label: 'Đã hủy', value: 'FAILED' }
  ];

  // Dialog & Detail Variables
  invoiceDialog: boolean = false;
  currentInvoice: InvoiceResponse | null = null;

  invoiceDetails: InvoiceDetailResponse[] = []; // Dữ liệu hiển thị trong bảng
  selectedInvoiceItems: InvoiceDetailResponse[] = []; // Dữ liệu dùng để IN ẤN

  loadingDetails: boolean = false;
  selectedTotalAmount: number = 0;
  today: Date = new Date();

  // [NEW] ADD ITEM VARIABLES (Biến cho chức năng Thêm Thuốc/Dịch vụ)
  addItemType: 'SERVICE' | 'DRUG' = 'SERVICE';
  servicesList: any[] = [];
  drugsList: any[] = [];
  selectedItemId: number | null = null;
  addItemQuantity: number = 1;

  // Services
  private billingService = inject(BillingService);
  private masterDataService = inject(MasterDataService);
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);

  constructor() { }

  ngOnInit() {
    this.loadInvoices();
    this.loadMasterData(); // Tải danh sách thuốc/dịch vụ để dùng cho dropdown
  }

  // --- 1. LOAD DANH SÁCH HÓA ĐƠN ---
  loadInvoices(event?: any) {
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

    this.billingService.getInvoices(
      this.page, this.size,
      this.selectedStatus || undefined,
      undefined,
      fromDateStr, toDateStr,
      this.keyword
    ).subscribe({
      next: (res) => {
        let data = res.result?.data || [];

        // Lọc Client-side cho loại hóa đơn (nếu Backend chưa hỗ trợ)
        if (this.selectedType) {
            data = data.filter((i: any) => i.type === this.selectedType);
        }

        this.invoices = data;
        this.totalRecords = res.result?.totalElements || 0;
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.showError(err);
      }
    });
  }

  // Tải danh sách Dịch vụ & Thuốc cho Dropdown
  loadMasterData() {
      this.masterDataService.getAllServices(1, 1000).subscribe({
          next: (res) => {
              this.servicesList = res.result?.data.map((s: any) => ({
                  label: `${s.name} (${this.formatCurrency(s.price)})`,
                  value: s.serviceId
              })) || [];
          }
      });

      this.masterDataService.getDrugs(1, 1000).subscribe({
          next: (res) => {
              this.drugsList = res.result?.data.map((d: any) => ({
                  label: `${d.name} (${d.unit}) - ${this.formatCurrency(d.price)}`,
                  value: d.drugId
              })) || [];
          }
      });
  }

  onGlobalFilter(event: any) {
    clearTimeout(this.typingTimer);
    this.typingTimer = setTimeout(() => {
      this.keyword = event.target.value;
      this.loadInvoices();
    }, 500);
  }

  onStatusChange() {
      this.loadInvoices();
  }

  // --- 2. XEM CHI TIẾT & LOGIC DÒNG ẢO ---
  openInvoiceDetail(invoice: InvoiceResponse) {
    this.currentInvoice = invoice;
    this.invoiceDialog = true;
    this.invoiceDetails = [];
    this.selectedInvoiceItems = [];
    this.loadingDetails = true;

    // Reset form thêm item
    this.selectedItemId = null;
    this.addItemQuantity = 1;

    // 1. Chuẩn bị API lấy chi tiết hóa đơn hiện tại (FINAL)
    const currentDetails$ = this.billingService.getDetailsByInvoice(invoice.invoiceId);

    // 2. Chuẩn bị API lấy chi tiết hóa đơn cọc (BOOKING) - Nếu có ID
    let bookingDetails$ = of({ result: [] }); // Mặc định là rỗng nếu không có cọc
    if (invoice.type === 'FINAL' && invoice.bookingInvoiceId) {
        // Gọi API lấy chi tiết của hóa đơn cọc
        bookingDetails$ = this.billingService.getDetailsByInvoice(invoice.bookingInvoiceId) as any;
    }

    // 3. Chạy song song cả 2 API
    forkJoin([currentDetails$, bookingDetails$]).subscribe({
        next: ([currentRes, bookingRes]: [any, any]) => {
            let finalDetails = currentRes.result || [];
            const bookingDetails = bookingRes.result || [];

            // --- LOGIC GỘP DÒNG CỌC VÀO HÓA ĐƠN CUỐI ---
            if (invoice.type === 'FINAL' && bookingDetails.length > 0) {
                const depositTotal = invoice.depositAmount || 0;

                // A. Biến đổi chi tiết cọc: Đánh dấu để hiển thị màu khác
                const mappedBookingDetails = bookingDetails.map((d: any) => ({
                    ...d,
                    detailId: -d.detailId, // Đổi sang ID âm để không bị trùng ID với DB và chặn xóa
                    serviceName: `(Đã cọc) ${d.serviceName || d.drugName || 'Phí đặt lịch'}`,
                    isPrePaid: true // Flag để tô màu vàng ở giao diện (nếu muốn)
                }));

                // B. Tạo dòng "Khấu trừ cọc" (Dòng ảo để trừ tiền)
                const deductionLine = {
                    detailId: -9999, // ID đặc biệt
                    serviceName: 'Khấu trừ cọc đã đóng',
                    quantity: 1,
                    unitPrice: -depositTotal, // Giá trị ÂM để trừ tiền
                    drugName: null,
                    isDeduction: true // Flag để tô màu đỏ
                };

                // C. Gộp tất cả lại: [Chi tiết cọc] + [Dòng khấu trừ] + [Chi tiết phát sinh thực tế]
                finalDetails = [...mappedBookingDetails, deductionLine, ...finalDetails];
            }

            this.invoiceDetails = finalDetails;

            // Mặc định chọn tất cả để in
            this.selectedInvoiceItems = [...finalDetails];

            this.calculateTotal();
            this.loadingDetails = false;
        },
        error: (err) => {
            this.loadingDetails = false;
            this.showError(err);
        }
    });
}

  // --- 3. THÊM / XÓA CHI TIẾT ---
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
              if (this.currentInvoice) this.openInvoiceDetail(this.currentInvoice);
          },
          error: (err) => this.showError(err)
      });
  }

  deleteInvoiceDetail(detailId: number) {
    if (detailId < 0) {
        this.messageService.add({severity: 'warn', summary: 'Không thể xóa', detail: 'Đây là dòng thông tin cọc.'});
        return;
    }

    this.confirmationService.confirm({
      message: 'Xóa mục này khỏi hóa đơn?',
      header: 'Xác nhận',
      icon: 'pi pi-trash',
      acceptLabel: 'Xóa',
      acceptButtonStyleClass: 'p-button-danger',
      rejectLabel: 'Hủy',
      accept: () => {
        this.billingService.deleteInvoiceDetail(detailId).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Đã xóa', detail: 'Cập nhật thành công' });
            if (this.currentInvoice) this.openInvoiceDetail(this.currentInvoice);
          },
          error: (err) => this.showError(err)
        });
      }
    });
  }

  // --- 4. THANH TOÁN ---
  confirmPayment() {
      if (!this.currentInvoice) return;

      this.confirmationService.confirm({
        message: `Xác nhận thu tiền mặt: <b>${this.formatCurrency(this.currentInvoice.totalAmount)}</b>?`,
        header: 'Xác nhận thanh toán',
        icon: 'pi pi-wallet',
        acceptLabel: 'Đồng ý',
        rejectLabel: 'Hủy',
        acceptButtonStyleClass: 'p-button-success',
        accept: () => {
          // Gọi API update trạng thái hóa đơn (Cần đảm bảo API hỗ trợ)
          const request: any = { paymentStatus: 'PAID', paymentMethod: 'CASH' };

          // Giả sử service có hàm updateInvoice, nếu không thì cần bổ sung vào service
          // Ở đây tôi dùng tạm hàm create hoặc endpoint update nếu có
           // *Lưu ý: Nếu BillingService chưa có hàm updateInvoice, bạn cần thêm vào nhé.*
           // Tạm thời gọi API mẫu:
           this.billingService.updateInvoice(this.currentInvoice!.invoiceId, request).subscribe({
               next: () => {
                   this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Thanh toán hoàn tất!' });
                   this.invoiceDialog = false;
                   this.loadInvoices();
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
                  this.showError({error: {message: 'Không lấy được link thanh toán'}});
              }
              this.loadingDetails = false;
          },
          error: (err) => {
              this.showError(err);
              this.loadingDetails = false;
          }
      });
  }

  // --- 5. XÓA HÓA ĐƠN ---
  deleteInvoice(invoice: InvoiceResponse) {
      this.confirmationService.confirm({
          message: `Bạn có chắc muốn hủy hóa đơn <b>${invoice.transactionCode || invoice.invoiceId}</b>?`,
          header: 'Xác nhận hủy',
          icon: 'pi pi-exclamation-triangle',
          acceptLabel: 'Hủy hóa đơn',
          acceptButtonStyleClass: 'p-button-danger',
          rejectLabel: 'Quay lại',
          accept: () => {
              this.billingService.deleteInvoice(invoice.invoiceId).subscribe({
                  next: () => {
                      this.messageService.add({ severity: 'success', summary: 'Đã hủy', detail: 'Hóa đơn đã được xóa.' });
                      this.loadInvoices();
                  },
                  error: (err) => this.showError(err)
              });
          }
      });
  }

  // --- HELPER FUNCTIONS ---
  calculateTotal() {
    this.selectedTotalAmount = this.invoiceDetails.reduce((sum, item) => {
        return sum + (item.quantity * item.unitPrice);
    }, 0);
  }

  showError(err: any) {
    const msg = err.error?.message || 'Có lỗi xảy ra!';
    this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: msg });
  }

  formatCurrency(amount: number): string {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  }

  getItemName(item: InvoiceDetailResponse): string {
    return item.serviceName || item.drugName || 'Không xác định';
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

  getPaymentMethodConfig(method: string): any {
    switch (method) {
        case 'VNPAY': return { label: 'VNPAY', class: 'method-vnpay', icon: 'pi pi-qrcode' };
        case 'CASH': return { label: 'Tiền mặt', class: 'method-cash', icon: 'pi pi-wallet' };
        default: return { label: method || '---', class: 'surface-200 text-600', icon: 'pi pi-info-circle' };
    }
  }

  getInvoiceTypeConfig(type: string): any {
      switch (type) {
          case 'BOOKING': return { label: 'Phí đặt lịch', severity: 'warning', icon: 'pi pi-clock' };
          case 'FINAL': return { label: 'Tất toán', severity: 'success', icon: 'pi pi-check-circle' };
          default: return { label: type, severity: 'info', icon: 'pi pi-info-circle' };
      }
  }

  printInvoice() {
    window.print();
  }
}
