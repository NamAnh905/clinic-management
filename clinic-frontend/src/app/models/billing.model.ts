import { PaymentMethod, PaymentStatus } from "./core.model";

export interface InvoiceDetailResponse {
  detailId: number;
  serviceId?: number;
  serviceName?: string;
  drugId?: number;
  drugName?: string;
  quantity: number;
  unitPrice: number;
}

export interface InvoiceResponse {
  invoiceId: number;
  appointmentId: number;
  patientName: string;
  doctorName: string;
  totalAmount: number;
  paymentStatus: PaymentStatus;
  paymentMethod: PaymentMethod;
  transactionCode?: string;
  createdAt?: string;
}

export interface InvoiceCreationRequest {
  appointmentId: number;
  totalAmount?: number;
  paymentStatus: PaymentStatus;
  paymentMethod: PaymentMethod;
  transactionCode?: string;
  serviceIds?: number[]; // List dịch vụ khám thêm (nếu có)
}

export interface InvoiceUpdateRequest {
  totalAmount?: number;
  paymentStatus?: PaymentStatus;
  paymentMethod?: PaymentMethod;
  transactionCode?: string;
  serviceIds?: number[];
}

export interface InvoiceDetailCreationRequest {
  invoiceId: number;
  serviceId?: number;
  drugId?: number;
  quantity: number;
  unitPrice?: number;
}
