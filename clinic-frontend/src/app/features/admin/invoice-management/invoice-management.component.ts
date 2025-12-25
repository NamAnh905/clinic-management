import { Component, OnInit, ViewChild, inject } from '@angular/core';
import { CommonModule, formatDate } from '@angular/common';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { BillingService } from '../../../core/services/billing.service';
import { MasterDataService } from '../../../core/services/master-data.service'; // Mới thêm
import { InvoiceResponse, InvoiceDetailResponse, InvoiceUpdateRequest, InvoiceDetailCreationRequest } from '../../../models/billing.model';
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
import { RadioButtonModule } from 'primeng/radiobutton'; // Mới thêm
import { InputNumberModule } from 'primeng/inputnumber'; // Mới thêm

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

  // --- MAIN TABLE DATA ---
  invoices: InvoiceResponse[] = [];
  totalRecords: number = 0;
  loading: boolean = false;

  // --- FILTERS ---
  keyword: string = '';
  rangeDates: Date[] | undefined;
  selectedStatus: PaymentStatus | undefined;
  page: number = 1;
  size: number = 10;
  typingTimer: any;

  statusOptions = [
    { label: 'Tất cả', value: null },
    { label: 'Đã thanh toán', value: PaymentStatus.PAID },
    { label: 'Chờ thanh toán', value: PaymentStatus.PENDING },
    { label: 'Đã hủy/Lỗi', value: PaymentStatus.FAILED }
  ];

  // --- DETAIL DIALOG ---
  invoiceDialog: boolean = false;
  currentInvoice: InvoiceResponse | null = null;
  invoiceDetails: InvoiceDetailResponse[] = [];
  selectedInvoiceItems: InvoiceDetailResponse[] = []; // Các mục được chọn để in

  loadingDetails: boolean = false;
  today: Date = new Date();

  // --- ADD ITEM VARIABLES (MỚI) ---
  addItemType: 'SERVICE' | 'DRUG' = 'SERVICE';
  servicesList: any[] = [];
  drugsList: any[] = [];
  selectedItemId: number | null = null;
  addItemQuantity: number = 1;

  // Inject Services
  private billingService = inject(BillingService);
  private masterDataService = inject(MasterDataService);
  private messageService = inject(MessageService);
  private confirmationService = inject(ConfirmationService);

  ngOnInit() {
      // Tải sẵn danh sách thuốc/dịch vụ để dùng cho dropdown
      this.loadMasterData();
  }

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

  // --- LOAD INVOICES ---
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
        this.page, this.size, this.selectedStatus, undefined, fromDateStr, toDateStr, this.keyword
    ).subscribe({
        next: (res) => {
          this.invoices = res.result?.data || [];
          this.totalRecords = res.result?.totalElements || 0;
          this.loading = false;
        },
        error: () => {
            this.loading = false;
            this.invoices = [];
        }
      });
  }

  onGlobalFilter(event: any) {
    clearTimeout(this.typingTimer);
    this.typingTimer = setTimeout(() => {
      this.keyword = event.target.value;
      this.resetPaginator();
    }, 500);
  }

  onStatusChange() {
    this.resetPaginator();
  }

  resetPaginator() {
      if (this.dt) this.dt.reset();
  }

  // --- DIALOG DETAIL LOGIC ---

  openInvoiceDetail(invoice: InvoiceResponse) {
      this.currentInvoice = invoice;
      this.invoiceDialog = true;
      this.loadingDetails = true;
      this.invoiceDetails = [];
      this.selectedInvoiceItems = [];

      // Reset form thêm mới
      this.selectedItemId = null;
      this.addItemQuantity = 1;

      this.billingService.getDetailsByInvoice(invoice.invoiceId).subscribe({
          next: (res) => {
              this.invoiceDetails = res.result || [];
              // Mặc định chọn tất cả để in
              this.selectedInvoiceItems = [...this.invoiceDetails];
              this.loadingDetails = false;
          },
          error: () => {
              this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không tải được chi tiết hóa đơn' });
              this.loadingDetails = false;
          }
      });
  }

  // --- HÀM THÊM MỚI (MỚI) ---
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
              // Reset form
              this.selectedItemId = null;
              this.addItemQuantity = 1;
              // Reload detail
              if (this.currentInvoice) this.openInvoiceDetail(this.currentInvoice);
          },
          error: (err) => {
              this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: err.error?.message || 'Không thể thêm mục này' });
          }
      });
  }

  // --- HÀM XÓA CHI TIẾT (MỚI) ---
  deleteInvoiceDetail(detailId: number) {
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
                      this.messageService.add({ severity: 'success', summary: 'Đã xóa', detail: 'Đã xóa mục khỏi hóa đơn' });
                      if (this.currentInvoice) this.openInvoiceDetail(this.currentInvoice);
                  },
                  error: (err) => this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: err.error?.message })
              });
          }
     });
  }

  deleteInvoice(invoice: InvoiceResponse) {
      this.confirmationService.confirm({
          message: `Bạn có chắc chắn muốn hủy hóa đơn <b>#${invoice.transactionCode || invoice.invoiceId}</b>?<br>Hành động này sẽ hoàn trả thuốc vào kho (nếu có).`,
          header: 'Xác nhận hủy',
          icon: 'pi pi-exclamation-triangle',
          acceptLabel: 'Đồng ý Hủy',
          rejectLabel: 'Quay lại',
          acceptButtonStyleClass: 'p-button-danger',
          rejectButtonStyleClass: 'p-button-text',
          accept: () => {
              this.billingService.deleteInvoice(invoice.invoiceId).subscribe({
                  next: () => {
                      this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã xóa hóa đơn' });
                      this.loadInvoices();
                  },
                  error: (err) => {
                      this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: err.error?.message || 'Không thể xóa' });
                  }
              });
          }
      });
  }

  confirmPayment() {
    if (!this.currentInvoice) return;

    this.confirmationService.confirm({
      message: `Xác nhận thanh toán cho hóa đơn <b>#${this.currentInvoice.transactionCode || this.currentInvoice.invoiceId}</b>?<br>Tổng tiền: <b class="text-primary">${this.formatCurrency(this.currentInvoice.totalAmount)}</b>`,
      header: 'Xác nhận thu tiền',
      icon: 'pi pi-wallet',
      acceptLabel: 'Thanh toán',
      rejectLabel: 'Hủy',
      acceptButtonStyleClass: 'p-button-success',
      accept: () => {
        const request: InvoiceUpdateRequest = {
          paymentStatus: PaymentStatus.PAID,
          paymentMethod: PaymentMethod.CASH
        };

        this.billingService.updateInvoice(this.currentInvoice!.invoiceId, request).subscribe({
          next: () => {
            this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Thanh toán thành công!' });
            this.invoiceDialog = false;
            this.loadInvoices();
          },
          error: (err) => {
            this.messageService.add({ severity: 'error', summary: 'Thất bại', detail: err.error?.message });
          }
        });
      }
    });
  }

  // --- HELPERS ---

  get selectedTotalAmount(): number {
      if (!this.selectedInvoiceItems) return 0;
      return this.selectedInvoiceItems.reduce((sum, item) => sum + (item.quantity * item.unitPrice), 0);
  }

  printInvoice() {
      if (!this.currentInvoice) return;
      if (!this.selectedInvoiceItems || this.selectedInvoiceItems.length === 0) {
          this.messageService.add({ severity: 'warn', summary: 'Cảnh báo', detail: 'Vui lòng chọn ít nhất 1 mục để in!' });
          return;
      }

      this.today = new Date();
      setTimeout(() => {
          window.print();
      }, 100);
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
}
