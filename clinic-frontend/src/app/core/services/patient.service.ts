import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../../models/core.model';
import { PatientResponse, PatientCreationRequest, PatientUpdationRequest } from '../../models/patient.model';

@Injectable({
  providedIn: 'root'
})
export class PatientService {
  private baseUrl = `${environment.apiUrl}/patients`;

  constructor(private http: HttpClient) { }

  getPatients(page: number, size: number, keyword?: string): Observable<ApiResponse<PageResponse<PatientResponse>>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (keyword) params = params.set('keyword', keyword);

    // Angular sẽ tự build thành: /patients?page=0&size=10&keyword=NguyenVanA
    return this.http.get<ApiResponse<PageResponse<PatientResponse>>>(this.baseUrl, { params });
  }

  getPatientById(id: number): Observable<ApiResponse<PatientResponse>> {
    return this.http.get<ApiResponse<PatientResponse>>(`${this.baseUrl}/${id}`);
  }

  // Thường dùng cho Receptionist tạo hồ sơ
  createPatient(request: PatientCreationRequest): Observable<ApiResponse<PatientResponse>> {
    return this.http.post<ApiResponse<PatientResponse>>(this.baseUrl, request);
  }

  updatePatient(id: number, request: PatientUpdationRequest): Observable<ApiResponse<PatientResponse>> {
    return this.http.put<ApiResponse<PatientResponse>>(`${this.baseUrl}/${id}`, request);
  }

  deletePatient(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${id}`);
  }
}
