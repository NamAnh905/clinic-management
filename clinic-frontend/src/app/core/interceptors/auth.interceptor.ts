import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';

function isJwtExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1] || ''));
    if (!payload?.exp) return false;
    return Date.now() / 1000 >= payload.exp;
  } catch { return false; }
}

@Injectable()
export class TokenInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (req.method === 'OPTIONS') return next.handle(req);

    let path = '';
    try {
      const url = req.url.startsWith('http') ? new URL(req.url) : new URL(req.url, location.origin);
      path = url.pathname || '';
    } catch { return next.handle(req); }

    // ĐIỂM SỬA: Tách rõ các API Auth public không cần token
    const isAuthPublic = [
      /^\/api\/auth\/login(?:\/|$)/,
      /^\/api\/auth\/register(?:\/|$)/,
      /^\/api\/auth\/refresh(?:\/|$)/
    ].some(r => r.test(path));

    const isSwagger = path.startsWith('/swagger-ui') || path.startsWith('/v3/api-docs');

    const isPublic = [
      /^\/api\/cinemas\/public(?:\/|$)/,
      /^\/api\/showtimes\/public(?:\/|$)/,
      /^\/api\/movies(?:\/|$)/,
      /^\/api\/genres(?:\/|$)/,
      /^\/uploads(?:\/|$)/
    ].some(r => r.test(path));

    // Nếu lọt vào danh sách public này thì cho đi qua luôn, KHÔNG gắn token
    if (isAuthPublic || isSwagger || isPublic) {
      return next.handle(req);
    }

    // Các path còn lại (bao gồm cả /api/auth/me và /api/auth/logout) SẼ được gắn token
    const raw = (localStorage.getItem('accessToken') || '').trim();
    if (!raw || raw === 'null' || raw === 'undefined' || isJwtExpired(raw)) {
      return next.handle(req);
    }

    const authReq = req.clone({ setHeaders: { Authorization: `Bearer ${raw}` } });
    return next.handle(authReq);
  }
}
