import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TableModule } from 'primeng/table';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { BillingService } from '../../../api/billing.service';
import { InvoiceResponse, InvoiceDetailResponse } from '../../../models/billing.model';

@Component({
  selector: 'app-user-invoice',
  standalone: true,
  imports: [CommonModule, TableModule, DialogModule, ButtonModule],
  templateUrl: './user-invoice.component.html',
  styleUrls: ['./user-invoice.component.scss']
})
export class UserInvoiceComponent implements OnInit {
  invoices: InvoiceResponse[] = [];
  loading: boolean = false;
  totalRecords: number = 0;
  page: number = 0;
  size: number = 5;

  // Dialog Chi tiết
  showDetailDialog: boolean = false;
  selectedInvoice: InvoiceResponse | null = null;
  invoiceDetails: InvoiceDetailResponse[] = [];
  loadingDetails: boolean = false;

  private billingService = inject(BillingService);

  ngOnInit() {
    this.loadInvoices();
  }

  loadInvoices() {
    // 1. Dùng Cache để hiển thị ngay
    if (this.billingService.invoicesCache && this.billingService.invoicesCache.length > 0) {
        this.invoices = this.billingService.invoicesCache;
        this.totalRecords = this.billingService.totalInvoicesRecord;
        this.loading = false; // Tắt loading ngay
    } else {
        this.loading = true; // Chỉ hiện loading lần đầu
    }

    const keyword = '';

    // 2. Gọi API ngầm (Silent Update)
    this.billingService.getInvoices(
      this.page + 1,
      this.size,
      undefined, undefined, undefined, undefined,
      keyword, 'createdAt', 'desc'
    ).subscribe({
      next: (res) => {
        if (res.result) {
          this.invoices = (res.result as any).data || [];
          this.totalRecords = res.result.totalElements;
          // (Service đã tự update cache)
        }
        this.loading = false;
      },
      error: () => this.loading = false
    });
  }

  viewDetail(invoice: InvoiceResponse) {
    this.selectedInvoice = invoice;
    this.showDetailDialog = true;
    this.loadingDetails = true;
    this.invoiceDetails = [];

    this.billingService.getDetailsByInvoice(invoice.invoiceId).subscribe({
      next: (res) => {
        if (res.result) this.invoiceDetails = res.result;
        this.loadingDetails = false;
      },
      error: () => this.loadingDetails = false
    });
  }

  onPageChange(event: any) {
    this.page = event.first / event.rows;
    this.size = event.rows;
    this.loadInvoices();
  }

  getStatusLabel(status: string): string {
    switch (status) {
      case 'PAID': return 'Đã thanh toán';
      case 'UNPAID': return 'Chưa thanh toán';
      case 'REFUNDED': return 'Đã hoàn tiền';
      case 'CANCELLED': return 'Đã hủy';
      default: return status;
    }
  }
}
