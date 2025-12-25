import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { ApiResponse, PageResponse } from '../../models/core.model';
import { UserResponse } from '../../models/user.model';
import { map } from 'rxjs/operators';
import { UserUpdateRequest, RegisterRequest } from '../../models/auth.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private baseUrl = `${environment.apiUrl}/users`;

  constructor(private http: HttpClient) { }

  register(request: RegisterRequest): Observable<ApiResponse<UserResponse>> {
    return this.http.post<ApiResponse<UserResponse>>(this.baseUrl, request);
  }

  getUsers(page: number, size: number,status?: boolean | null, roleName?: string | null, keyword?: string): Observable<ApiResponse<PageResponse<UserResponse>>> {
    let params = new HttpParams()
      .set('page', page)
      .set('size', size);

    if (status !== null && status !== undefined) {
        params = params.set('status', status);
    }
    if (roleName) {
        params = params.set('roleName', roleName);
    }
    if (keyword) params = params.set('keyword', keyword);

    return this.http.get<ApiResponse<PageResponse<UserResponse>>>(this.baseUrl, { params });
  }

  getUserById(userId: number): Observable<ApiResponse<UserResponse>> {
    return this.http.get<ApiResponse<UserResponse>>(`${this.baseUrl}/${userId}`);
  }

  getMyInfo(): Observable<ApiResponse<UserResponse>> {
    return this.http.get<ApiResponse<UserResponse>>(`${this.baseUrl}/me`);
  }

  updateMyInfo(request: UserUpdateRequest): Observable<ApiResponse<UserResponse>> {
    return this.http.put<ApiResponse<UserResponse>>(`${this.baseUrl}/me`, request);
  }

  updateUser(userId: number, request: UserUpdateRequest): Observable<ApiResponse<UserResponse>> {
    return this.http.put<ApiResponse<UserResponse>>(`${this.baseUrl}/${userId}`, request);
  }

  deleteUser(userId: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.baseUrl}/${userId}`);
  }

  exportUsers() {
    // Không cần truyền tham số gì cả
    // Lưu ý: Nhớ sửa đúng đường dẫn api (bỏ chữ /users thừa nếu có)
    return this.http.get(`${this.baseUrl}/export`, {
        responseType: 'blob'
    });
  }
}
