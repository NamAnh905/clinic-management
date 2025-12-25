import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../../models/core.model';
import {
  MedicalRecordResponse, MedicalRecordCreationRequest, MedicalRecordUpdationRequest,
  PrescriptionResponse, PrescriptionCreationRequest, PrescriptionUpdateRequest,
  PresDetailCreationRequest, PresDetailUpdateRequest
} from '../../models/medical.model';
import { PresDetailResponse } from '../../models/medical.model';

@Injectable({
  providedIn: 'root'
})
export class MedicalService {
  private baseUrl = `${environment.apiUrl}`;

  constructor(private http: HttpClient) { }

  // ================== MEDICAL RECORDS (BỆNH ÁN) ==================

  createRecord(request: MedicalRecordCreationRequest): Observable<ApiResponse<MedicalRecordResponse>> {
    return this.http.post<ApiResponse<MedicalRecordResponse>>(`${this.baseUrl}/records`, request);
  }

  getMedicalRecords(
    page: number,
    size: number,
    keyword?: string,
    startDate?: string,
    endDate?: string,
    doctorId?: number
  ): Observable<ApiResponse<PageResponse<MedicalRecordResponse>>> {

    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (keyword) params = params.set('keyword', keyword);
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    if (doctorId) params = params.set('doctorId', doctorId);

    return this.http.get<ApiResponse<PageResponse<MedicalRecordResponse>>>(`${this.baseUrl}/records`, { params });
  }

  getAllPatients(): Observable<ApiResponse<any>> {
    return this.http.get<ApiResponse<any>>(`${environment.apiUrl}/patients?page=1&size=100`);
  }

  getEligibleAppointments(patientId: number): Observable<ApiResponse<PageResponse<any>>> {
      let params = new HttpParams()
        .set('patientId', patientId)       // Lọc theo bệnh nhân đã chọn
        .set('status', 'CONFIRMED')        // Chỉ lấy lịch đã xác nhận
        .set('page', 1)
        .set('size', 50);                  // Lấy 50 lịch gần nhất để đổ vào Dropdown

      return this.http.get<ApiResponse<PageResponse<any>>>(`${environment.apiUrl}/appointments`, { params });
  }

  updateRecord(recordId: number, request: MedicalRecordUpdationRequest): Observable<ApiResponse<MedicalRecordResponse>> {
    return this.http.put<ApiResponse<MedicalRecordResponse>>(`${this.baseUrl}/records/${recordId}`, request);
  }

  getRecordById(recordId: number): Observable<ApiResponse<MedicalRecordResponse>> {
    return this.http.get<ApiResponse<MedicalRecordResponse>>(`${this.baseUrl}/records/${recordId}`);
  }

  deleteRecord(recordId: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/records/${recordId}`);
  }
  // ================== PRESCRIPTIONS (ĐƠN THUỐC) ==================

  createPrescription(request: PrescriptionCreationRequest): Observable<ApiResponse<PrescriptionResponse>> {
    return this.http.post<ApiResponse<PrescriptionResponse>>(`${this.baseUrl}/prescriptions`, request);
  }

  getPrescriptionByRecord(recordId: number): Observable<ApiResponse<PrescriptionResponse>> {
    return this.http.get<ApiResponse<PrescriptionResponse>>(`${this.baseUrl}/prescriptions/record/${recordId}`);
  }

  updatePrescription(prescriptionId: number, request: PrescriptionUpdateRequest): Observable<ApiResponse<PrescriptionResponse>> {
    return this.http.put<ApiResponse<PrescriptionResponse>>(`${this.baseUrl}/prescriptions/${prescriptionId}`, request);
  }

  // ================== PRESCRIPTION DETAILS (CHI TIẾT THUỐC) ==================

  getPrescriptionDetails(prescriptionId: number): Observable<ApiResponse<PresDetailResponse[]>> {
    return this.http.get<ApiResponse<PresDetailResponse[]>>(`${this.baseUrl}/pres-detail/by-prescription/${prescriptionId}`);
  }

  addDrugToPrescription(request: PresDetailCreationRequest): Observable<ApiResponse<PresDetailResponse>> {
    return this.http.post<ApiResponse<PresDetailResponse>>(`${this.baseUrl}/pres-detail`, request);
  }

  updateDrugInPrescription(detailId: number, request: PresDetailUpdateRequest): Observable<ApiResponse<PresDetailResponse>> {
    return this.http.put<ApiResponse<PresDetailResponse>>(`${this.baseUrl}/pres-detail/${detailId}`, request);
  }

  removeDrugFromPrescription(detailId: number): Observable<ApiResponse<void>> {
      return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/pres-detail/${detailId}`);
  }
}
