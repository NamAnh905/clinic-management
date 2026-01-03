import { HttpErrorResponse, HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, catchError, filter, switchMap, take, throwError, Observable } from 'rxjs';
import { AuthService } from '../services/auth.service'; // Đảm bảo đường dẫn đúng
import { ApiResponse } from '../../models/core.model'; // Đảm bảo đường dẫn đúng
import { AuthenticationResponse } from '../../models/auth.model'; // Đảm bảo đường dẫn đúng

// Biến global quản lý trạng thái refresh token
let isRefreshing = false;
const refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(null);

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const token = localStorage.getItem('token');

  // --- 1. ĐỊNH NGHĨA CÁC ENDPOINT PUBLIC (WHITELIST) ---
  // Các API Auth cơ bản
  const isAuthApi = req.url.includes('/auth/login') ||
                    req.url.includes('/auth/introspect') ||
                    req.url.includes('/auth/logout') ||
                    req.url.includes('/auth/refresh');

  const isRegisterApi = req.url.includes('/users') && req.method === 'POST';

  // [MỚI] Các API phục vụ Booking Public & Master Data cho khách vãng lai
  // Backend cần cấu hình permitAll() cho các path này
  const isBookingPublicApi = req.url.includes('/appointments/public') ||
                             (req.method === 'GET' && req.url.includes('/master-data')) || // Lấy chuyên khoa, dịch vụ
                             (req.method === 'GET' && req.url.includes('/staffs'));      // Lấy danh sách bác sĩ

  // Gom tất cả vào nhóm Public
  const isPublicEndpoint = isAuthApi || isRegisterApi || isBookingPublicApi;

  let authReq = req;

  // --- 2. LOGIC ĐÍNH KÈM TOKEN ---
  // Chỉ gửi Token nếu có VÀ không phải endpoint public
  // (Lý do: Nếu gửi token hết hạn vào API public, một số config Spring Security vẫn chặn 401.
  //  Tốt nhất với API public của Guest thì không gửi token để tránh rủi ro).
  if (token && !isPublicEndpoint) {
    authReq = addTokenHeader(req, token);
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // --- 3. XỬ LÝ LỖI 401 ---
      // Chỉ thực hiện Refresh Token nếu lỗi 401 xảy ra ở các API PRIVATE (yêu cầu đăng nhập)
      // Nếu API Public mà lỗi (ví dụ 400, 500) hoặc 401 giả (hiếm) thì cứ throw ra cho Component xử lý
      if (error.status === 401 && !isPublicEndpoint) {
        return handle401Error(authService, router, authReq, next);
      }
      return throwError(() => error);
    })
  );
};

/**
 * Helper: Clone request và thêm Header Authorization
 */
function addTokenHeader(request: HttpRequest<any>, token: string) {
  return request.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });
}

/**
 * Helper: Logic xử lý Refresh Token khi gặp lỗi 401
 */
function handle401Error(authService: AuthService, router: Router, request: HttpRequest<any>, next: HttpHandlerFn): Observable<any> {
  if (!isRefreshing) {
    isRefreshing = true;
    refreshTokenSubject.next(null);

    const oldToken = localStorage.getItem('token') || '';

    // Gọi API Refresh
    return authService.refreshToken(oldToken).pipe(
      switchMap((response: ApiResponse<AuthenticationResponse>) => {
        isRefreshing = false;

        const newToken = response.result.token;
        localStorage.setItem('token', newToken); // Lưu token mới
        refreshTokenSubject.next(newToken);      // Báo hiệu cho các request đang chờ

        // Thử lại request ban đầu với token mới
        return next(addTokenHeader(request, newToken));
      }),
      catchError((err) => {
        // Nếu refresh thất bại -> Logout
        isRefreshing = false;
        localStorage.removeItem('token');
        router.navigate(['/login']);
        return throwError(() => err);
      })
    );
  } else {
    // Nếu đang có tiến trình refresh chạy, các request khác xếp hàng đợi
    return refreshTokenSubject.pipe(
      filter(token => token !== null),
      take(1),
      switchMap((token) => next(addTokenHeader(request, token!)))
    );
  }
}
