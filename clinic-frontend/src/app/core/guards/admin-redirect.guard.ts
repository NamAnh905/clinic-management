// src/app/core/guards/admin-redirect.guard.ts
import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const AdminRedirectGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // 1. Nếu là Admin: Vào trang Quản lý người dùng
  if (authService.hasRole(['ADMIN'])) {
    return router.createUrlTree(['/admin/users']);
  }

  // 2. Nếu là Bác sĩ: Vào trang Lịch hẹn (hoặc Lịch làm việc tùy bạn)
  if (authService.hasRole(['DOCTOR'])) {
    return router.createUrlTree(['/admin/appointments']);
  }

  // 3. Nếu là Lễ tân: Vào trang Lịch hẹn
  if (authService.hasRole(['RECEPTIONIST'])) {
    return router.createUrlTree(['/admin/appointments']);
  }

  // 4. Nếu là Bệnh nhân (lỡ đi lạc vào đây): Đá về trang login
  return router.createUrlTree(['/login']);
};
