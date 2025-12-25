import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../../models/core.model';
import {
  ScheduleResponse,
  ScheduleCreationRequest,
  ScheduleUpdationRequest
} from '../../models/schedule.model';

@Injectable({
  providedIn: 'root'
})
export class ScheduleService {
  private baseUrl = `${environment.apiUrl}/schedules`;

  constructor(private http: HttpClient) { }

  getSchedules(
    page: number,
    size: number,
    doctorId?: number,
    receptionistId?: number,
    specialtyId?: number,
    startDate?: string,
    endDate?: string,
    viewType?: 'DOCTOR' | 'RECEPTIONIST'
  ): Observable<ApiResponse<PageResponse<ScheduleResponse>>> {

    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (doctorId) params = params.set('doctorId', doctorId);
    if (receptionistId) params = params.set('receptionistId', receptionistId); // Gửi lên BE
    if (specialtyId) params = params.set('specialtyId', specialtyId);
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    if (viewType) params = params.set('viewType', viewType);

    return this.http.get<ApiResponse<PageResponse<ScheduleResponse>>>(this.baseUrl, { params });
  }

  createSchedule(request: ScheduleCreationRequest): Observable<ApiResponse<ScheduleResponse>> {
    return this.http.post<ApiResponse<ScheduleResponse>>(this.baseUrl, request);
  }

  updateSchedule(scheduleId: number, request: ScheduleUpdationRequest): Observable<ApiResponse<ScheduleResponse>> {
    return this.http.put<ApiResponse<ScheduleResponse>>(`${this.baseUrl}/${scheduleId}`, request);
  }

  deleteSchedule(scheduleId: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${scheduleId}`);
  }
}
