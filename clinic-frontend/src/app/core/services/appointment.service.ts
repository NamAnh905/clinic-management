import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../../models/core.model';
import {
  AppointmentResponse,
  AppointmentCreationRequest, AppointmentUpdationRequest
} from '../../models/appointment.model';
import { AppointmentStatus } from '../../models/core.model';

@Injectable({
  providedIn: 'root'
})
export class AppointmentService {
  private baseUrl = `${environment.apiUrl}/appointments`;

  constructor(private http: HttpClient) { }

  getAppointments(
    page: number,
    size: number,
    doctorId?: number,
    patientId?: number,
    keyword?: string,
    status?: AppointmentStatus,
    fromDate?: string,
    toDate?: string
  ): Observable<ApiResponse<PageResponse<AppointmentResponse>>> {

    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (doctorId) params = params.set('doctorId', doctorId);
    if (patientId) params = params.set('patientId', patientId);
    if (keyword) params = params.set('keyword', keyword);
    if (status) params = params.set('status', status);
    if (fromDate) params = params.set('startDate', fromDate);
    if (toDate) params = params.set('endDate', toDate);

    return this.http.get<ApiResponse<PageResponse<AppointmentResponse>>>(this.baseUrl, { params });
  }

  getAppointmentDetail(id: number): Observable<ApiResponse<AppointmentResponse>> {
    return this.http.get<ApiResponse<AppointmentResponse>>(`${this.baseUrl}/${id}`);
  }

  bookAppointment(request: AppointmentCreationRequest): Observable<ApiResponse<AppointmentResponse>> {
    return this.http.post<ApiResponse<AppointmentResponse>>(this.baseUrl, request);
  }

  updateAppointment(id: number, request: AppointmentUpdationRequest): Observable<ApiResponse<AppointmentResponse>> {
    return this.http.put<ApiResponse<AppointmentResponse>>(`${this.baseUrl}/${id}`, request);
  }

  exportAppointments(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/export`, {
      responseType: 'blob'
    });
  }
}
