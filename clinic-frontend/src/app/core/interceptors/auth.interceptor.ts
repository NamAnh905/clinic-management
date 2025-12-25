import { HttpErrorResponse, HttpInterceptorFn, HttpRequest, HttpHandlerFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, catchError, filter, switchMap, take, throwError, Observable } from 'rxjs';
import { AuthService } from '../services/auth.service'; // Điều chỉnh đường dẫn thực tế của bạn
import { ApiResponse } from '../../models/core.model';
import { AuthenticationResponse } from '../../models/auth.model';

// Biến để kiểm soát trạng thái refresh (đặt ngoài function để dùng chung giữa các request)
let isRefreshing = false;
const refreshTokenSubject: BehaviorSubject<string | null> = new BehaviorSubject<string | null>(null);

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);
  const token = localStorage.getItem('token');

  // Kiểm tra các endpoint công khai (không cần đính kèm token)
  const isAuthApi = req.url.includes('/auth/login') ||
                    req.url.includes('/auth/introspect') ||
                    req.url.includes('/auth/logout') ||
                    req.url.includes('/auth/refresh'); // Không chặn chính nó

  const isRegisterApi = req.url.includes('/users') && req.method === 'POST';
  const isPublicEndpoint = isAuthApi || isRegisterApi;

  let authReq = req;

  // Đính kèm Token vào Header nếu có và không phải endpoint công khai
  if (token && !isPublicEndpoint) {
    authReq = addTokenHeader(req, token);
  }

  return next(authReq).pipe(
    catchError((error: HttpErrorResponse) => {
      // Nếu lỗi 401 và không phải là lỗi từ các API public
      if (error.status === 401 && !isPublicEndpoint) {
        return handle401Error(authService, router, authReq, next);
      }
      return throwError(() => error);
    })
  );
};

/**
 * Hàm hỗ trợ đính kèm Token vào Header
 */
function addTokenHeader(request: HttpRequest<any>, token: string) {
  return request.clone({
    setHeaders: {
      Authorization: `Bearer ${token}`
    }
  });
}

/**
 * Logic xử lý khi gặp lỗi 401 (Refresh Token)
 */
function handle401Error(authService: AuthService, router: Router, request: HttpRequest<any>, next: HttpHandlerFn): Observable<any> {
  if (!isRefreshing) {
    isRefreshing = true;
    refreshTokenSubject.next(null);

    const oldToken = localStorage.getItem('token') || '';

    // Gọi API refresh ở AuthService
    return authService.refreshToken(oldToken).pipe(
      switchMap((response: ApiResponse<AuthenticationResponse>) => {
        isRefreshing = false;

        const newToken = response.result.token;
        localStorage.setItem('token', newToken); // Lưu token mới vào storage
        refreshTokenSubject.next(newToken); // Thông báo cho các request đang "chờ"

        // Thử lại request ban đầu với token mới
        return next(addTokenHeader(request, newToken));
      }),
      catchError((err) => {
        isRefreshing = false;
        // Nếu refresh cũng lỗi (quá thời hạn REFRESHABLE_DURATION), thực hiện logout
        localStorage.removeItem('token');
        router.navigate(['/login']);
        return throwError(() => err);
      })
    );
  } else {
    // Nếu đang có một request refresh khác đang chạy, cho request này "xếp hàng" chờ
    return refreshTokenSubject.pipe(
      filter(token => token !== null), // Đợi cho đến khi có token mới
      take(1),
      switchMap((token) => next(addTokenHeader(request, token!))) // Thử lại với token đó
    );
  }
}
