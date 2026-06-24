import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators'; // <--- Import thêm tap
import { environment } from '../../environments/environment';
import { ApiResponse, PageResponse } from '../models/core.model';
import {
  InvoiceResponse, InvoiceCreationRequest, InvoiceUpdateRequest,
  InvoiceDetailCreationRequest, InvoiceDetailResponse
} from '../models/billing.model';
import { PaymentMethod, PaymentStatus } from '../models/core.model';

@Injectable({
  providedIn: 'root'
})
export class BillingService {
  private invoiceUrl = `${environment.apiUrl}/invoices`;
  private detailUrl = `${environment.apiUrl}/invoice-details`;
  private paymentUrl = `${environment.apiUrl}/payment`;

  // --- CACHE VARIABLES ---
  public invoicesCache: InvoiceResponse[] | null = null;
  public totalInvoicesRecord: number = 0;

  constructor(private http: HttpClient) { }

  // ================== QUẢN LÝ HÓA ĐƠN ==================

  getInvoices(
    page: number,
    size: number,
    paymentStatus?: PaymentStatus,
    paymentMethod?: PaymentMethod,
    fromDate?: string,
    toDate?: string,
    keyword?: string,
    sortBy: string = 'createdAt',
    sortDir: string = 'desc'
  ): Observable<ApiResponse<PageResponse<InvoiceResponse>>> {

    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);

    if (paymentStatus) params = params.set('paymentStatus', paymentStatus);
    if (paymentMethod) params = params.set('paymentMethod', paymentMethod);
    if (fromDate) params = params.set('startDate', fromDate);
    if (toDate) params = params.set('endDate', toDate);

    if (keyword) params = params.set('keyword', keyword);

    return this.http.get<ApiResponse<PageResponse<InvoiceResponse>>>(this.invoiceUrl, { params })
      .pipe(
        tap(res => {
          // Lưu Cache khi thành công
          if (res.result) {
            this.invoicesCache = res.result.data;
            this.totalInvoicesRecord = res.result.totalElements;
          }
        })
      );
  }

  getInvoiceByAppointment(appointmentId: number): Observable<ApiResponse<InvoiceResponse>> {
    return this.http.get<ApiResponse<InvoiceResponse>>(`${this.invoiceUrl}/by-appointment/${appointmentId}`);
  }

  getInvoiceById(invoiceId: number): Observable<ApiResponse<InvoiceResponse>> {
    return this.http.get<ApiResponse<InvoiceResponse>>(`${this.invoiceUrl}/${invoiceId}`);
  }

  createInvoice(request: InvoiceCreationRequest): Observable<ApiResponse<InvoiceResponse>> {
    return this.http.post<ApiResponse<InvoiceResponse>>(this.invoiceUrl, request);
  }

  createBookingInvoice(appointmentId: number): Observable<ApiResponse<InvoiceResponse>> {
    return this.http.post<ApiResponse<InvoiceResponse>>(`${this.invoiceUrl}/booking/${appointmentId}`, {});
  }

  updateInvoice(invoiceId: number, request: InvoiceUpdateRequest): Observable<ApiResponse<InvoiceResponse>> {
    return this.http.put<ApiResponse<InvoiceResponse>>(`${this.invoiceUrl}/${invoiceId}`, request);
  }

  deleteInvoice(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.invoiceUrl}/${id}`);
  }

  // ================== CHI TIẾT DỊCH VỤ TRONG HÓA ĐƠN ==================

  addInvoiceDetail(request: InvoiceDetailCreationRequest): Observable<ApiResponse<InvoiceDetailResponse>> {
    return this.http.post<ApiResponse<InvoiceDetailResponse>>(this.detailUrl, request);
  }

  getDetailsByInvoice(invoiceId: number): Observable<ApiResponse<InvoiceDetailResponse[]>> {
    return this.http.get<ApiResponse<InvoiceDetailResponse[]>>(`${this.detailUrl}/by-invoice/${invoiceId}`);
  }

  deleteInvoiceDetail(detailId: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.detailUrl}/${detailId}`);
  }

  // ================== THANH TOÁN & THỐNG KÊ ==================

  initiateVnPayPayment(invoiceId: number): Observable<ApiResponse<string>> {
    return this.http.get<ApiResponse<string>>(`${environment.apiUrl}/payment/vnpay/${invoiceId}`);
  }

  processVnPayReturn(params: any): Observable<ApiResponse<InvoiceResponse>> {
    return this.http.get<ApiResponse<InvoiceResponse>>(`${this.paymentUrl}/vnpay-return`, { params });
  }

  getRevenueStatistics(year: number, month?: number): Observable<ApiResponse<any>> {
    let url = `${this.invoiceUrl}/statistics/revenue?year=${year}`;
    if (month) url += `&month=${month}`;
    return this.http.get<ApiResponse<any>>(url);
  }
}
