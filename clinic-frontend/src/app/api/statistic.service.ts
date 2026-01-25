import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { DashboardSummary } from '../models/dashboard.model';
import { ApiResponse } from '../models/core.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class StatisticService {
  private readonly API_URL = `${environment.apiUrl}/revenue`;

  constructor(private http: HttpClient) {}

  getRevenueStats(startDate: Date, endDate: Date): Observable<ApiResponse<DashboardSummary>> {
    let params = new HttpParams();

    if (startDate) {
      params = params.set('startDate', this.formatDate(startDate));
    }

    if (endDate) {
      params = params.set('endDate', this.formatDate(endDate));
    }

    return this.http.get<ApiResponse<DashboardSummary>>(`${this.API_URL}/dashboard`, { params });
  }

  private formatDate(date: Date): string {
    if (!date) return '';
    const year = date.getFullYear();
    const month = ('0' + (date.getMonth() + 1)).slice(-2);
    const day = ('0' + date.getDate()).slice(-2);
    return `${year}-${month}-${day}`;
  }
}
