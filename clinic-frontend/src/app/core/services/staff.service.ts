import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../../models/core.model';
import {
  DoctorResponse, DoctorCreationRequest, DoctorUpdateRequest,
  ReceptionistResponse, ReceptionistCreationRequest, ReceptionistUpdateRequest
} from '../../models/staff.model';

@Injectable({
  providedIn: 'root'
})
export class StaffService {
  private baseUrl = `${environment.apiUrl}`;

  constructor(private http: HttpClient) { }

  // ============ DOCTORS ============
  getAllDoctors(page: number, size: number, keyword?: string): Observable<ApiResponse<PageResponse<DoctorResponse>>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (keyword) {
      params = params.set('keyword', keyword);
    }

    return this.http.get<ApiResponse<PageResponse<DoctorResponse>>>(`${this.baseUrl}/doctors`, { params });
  }

  getDoctorById(id: number): Observable<ApiResponse<DoctorResponse>> {
    return this.http.get<ApiResponse<DoctorResponse>>(`${this.baseUrl}/doctors/${id}`);
  }

  getDoctorInfoByUserId(userId: number): Observable<any> {
      return this.http.get<ApiResponse<any>>(`${environment.apiUrl}/doctors/find-by-user/${userId}`)
        .pipe(
          map(response => response.result)
        );
  }

  createDoctor(request: DoctorCreationRequest): Observable<ApiResponse<DoctorResponse>> {
    return this.http.post<ApiResponse<DoctorResponse>>(`${this.baseUrl}/doctors`, request);
  }

  updateDoctor(id: number, request: DoctorUpdateRequest): Observable<ApiResponse<DoctorResponse>> {
    return this.http.put<ApiResponse<DoctorResponse>>(`${this.baseUrl}/doctors/${id}`, request);
  }

  deleteDoctor(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/doctors/${id}`);
  }

  // ============ RECEPTIONISTS ============
  createReceptionist(request: ReceptionistCreationRequest): Observable<ApiResponse<ReceptionistResponse>> {
     return this.http.post<ApiResponse<ReceptionistResponse>>(`${this.baseUrl}/receptionists`, request);
  }

  updateReceptionist(id: number, request: ReceptionistUpdateRequest): Observable<ApiResponse<ReceptionistResponse>> {
    return this.http.put<ApiResponse<ReceptionistResponse>>(`${this.baseUrl}/receptionists/${id}`, request);
  }

  getAllReceptionists(page: number, size: number, keyword?: string): Observable<ApiResponse<PageResponse<ReceptionistResponse>>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (keyword) {
      params = params.set('keyword', keyword);
    }

    return this.http.get<ApiResponse<PageResponse<ReceptionistResponse>>>(`${this.baseUrl}/receptionists`, { params });
  }

  getReceptionistById(id: number): Observable<ApiResponse<ReceptionistResponse>> {
    return this.http.get<ApiResponse<ReceptionistResponse>>(`${this.baseUrl}/receptionists/${id}`);
  }

  getReceptionistInfoByUserId(userId: number): Observable<any> {
      return this.http.get<ApiResponse<any>>(`${environment.apiUrl}/receptionists/find-by-user/${userId}`)
        .pipe(
          map(response => response.result)
        );
  }

  deleteReceptionist(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/receptionists/${id}`);
  }
}
