import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators'; // <--- Import thêm tap
import { environment } from '../../environments/environment';
import { ApiResponse, PageResponse } from '../models/core.model';
import {
  AppointmentResponse,
  AppointmentCreationRequest,
  AppointmentUpdationRequest,
  PublicAppointmentRequest,
  CancelAppointmentRequest
} from '../models/appointment.model';
import { AppointmentStatus } from '../models/core.model';

@Injectable({
  providedIn: 'root'
})
export class AppointmentService {
  private baseUrl = `${environment.apiUrl}/appointments`;

  // --- CACHE VARIABLE ---
  public appointmentsCache: AppointmentResponse[] | null = null;

  constructor(private http: HttpClient) { }

  getAppointments(
    page: number,
    size: number,
    doctorId?: number,
    patientId?: number,
    keyword?: string,
    status?: AppointmentStatus,
    fromDate?: string,
    toDate?: string,
    sortBy: string = 'appointmentTime',
    sortDir: string = 'desc'
  ): Observable<ApiResponse<PageResponse<AppointmentResponse>>> {

    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('sortBy', sortBy)
      .set('sortDir', sortDir);

    if (doctorId) params = params.set('doctorId', doctorId);
    if (patientId) params = params.set('patientId', patientId);
    if (keyword) params = params.set('keyword', keyword);
    if (status) params = params.set('status', status);
    if (fromDate) params = params.set('startDate', fromDate);
    if (toDate) params = params.set('endDate', toDate);

    return this.http.get<ApiResponse<PageResponse<AppointmentResponse>>>(this.baseUrl, { params })
      .pipe(
        tap(res => {
          // Khi API trả về thành công, lưu vào Cache
          if (res.result) {
            this.appointmentsCache = res.result.data;
          }
        })
      );
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

  getAvailableSlots(doctorId: number, date: string): Observable<ApiResponse<string[]>> {
    const params = new HttpParams()
      .set('doctorId', doctorId)
      .set('date', date);

    return this.http.get<ApiResponse<string[]>>(`${this.baseUrl}/public/available-slots`, { params });
  }

  bookPublicAppointment(request: PublicAppointmentRequest): Observable<ApiResponse<AppointmentResponse>> {
    return this.http.post<ApiResponse<AppointmentResponse>>(`${this.baseUrl}/public/booking`, request);
  }

  cancelPublicAppointment(request: CancelAppointmentRequest): Observable<ApiResponse<string>> {
    return this.http.post<ApiResponse<string>>(`${this.baseUrl}/public/cancel`, request);
  }

  exportAppointments(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/export`, {
      responseType: 'blob'
    });
  }
}
