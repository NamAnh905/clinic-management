import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../../models/core.model';
import {
  ServiceEntityResponse, SECreationRequest, SEUpdationRequest,
  DrugResponse, DrugCreationRequest, DrugUpdateRequest,
  SpecialtyResponse, SpecialtyCreationRequest, SpecialtyUpdateRequest
} from '../../models/master-data.model';
import { ServiceType } from '../../models/core.model';

@Injectable({
  providedIn: 'root'
})
export class MasterDataService {
  private baseUrl = `${environment.apiUrl}`;

  constructor(private http: HttpClient) { }

  // ================== 1. SERVICES (DỊCH VỤ KHÁM/XÉT NGHIỆM) ==================
  getAllServices(
    page: number,
    size: number,
    activeOnly: boolean = true,
    serviceType?: ServiceType,
    keyword?: string
  ): Observable<ApiResponse<PageResponse<ServiceEntityResponse>>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)
      .set('activeOnly', activeOnly);

    if (serviceType) params = params.set('serviceType', serviceType);
    if (keyword) params = params.set('keyword', keyword);

    return this.http.get<ApiResponse<PageResponse<ServiceEntityResponse>>>(`${this.baseUrl}/services`, { params });
  }

  createService(request: SECreationRequest): Observable<ApiResponse<ServiceEntityResponse>> {
    return this.http.post<ApiResponse<ServiceEntityResponse>>(`${this.baseUrl}/services`, request);
  }

  updateService(id: number, request: SEUpdationRequest): Observable<ApiResponse<ServiceEntityResponse>> {
    return this.http.put<ApiResponse<ServiceEntityResponse>>(`${this.baseUrl}/services/${id}`, request);
  }

  deleteService(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/services/${id}`);
  }

  // ================== 2. DRUGS (KHO THUỐC) ==================
  getDrugs(
    page: number,
    size: number,
    keyword?: string,
  ): Observable<ApiResponse<PageResponse<DrugResponse>>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size)

    if (keyword) params = params.set('keyword', keyword);

    return this.http.get<ApiResponse<PageResponse<DrugResponse>>>(`${this.baseUrl}/drugs`, { params });
  }

  createDrug(request: DrugCreationRequest): Observable<ApiResponse<DrugResponse>> {
    return this.http.post<ApiResponse<DrugResponse>>(`${this.baseUrl}/drugs`, request);
  }

  updateDrug(id: number, request: DrugUpdateRequest): Observable<ApiResponse<DrugResponse>> {
    return this.http.put<ApiResponse<DrugResponse>>(`${this.baseUrl}/drugs/${id}`, request);
  }

  deleteDrug(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/drugs/${id}`);
  }

  exportDrugs(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/drugs/export`, {
      responseType: 'blob'
    });
  }

  // ================== 3. SPECIALTIES (CHUYÊN KHOA) ==================
  getAllSpecialties(
    page: number,
    size: number,
    keyword?: string
  ): Observable<ApiResponse<PageResponse<SpecialtyResponse>>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (keyword) params = params.set('keyword', keyword);

    return this.http.get<ApiResponse<PageResponse<SpecialtyResponse>>>(`${this.baseUrl}/specialties`, { params });
  }

  createSpecialty(request: SpecialtyCreationRequest): Observable<ApiResponse<SpecialtyResponse>> {
    return this.http.post<ApiResponse<SpecialtyResponse>>(`${this.baseUrl}/specialties`, request);
  }

  updateSpecialty(id: number, request: SpecialtyUpdateRequest): Observable<ApiResponse<SpecialtyResponse>> {
    return this.http.put<ApiResponse<SpecialtyResponse>>(`${this.baseUrl}/specialties/${id}`, request);
  }

  deleteSpecialty(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/specialties/${id}`);
  }
}
