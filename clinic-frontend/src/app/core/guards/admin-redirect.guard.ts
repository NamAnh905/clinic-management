// src/app/core/guards/admin-redirect.guard.ts
import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../../api/auth.service';

export const AdminRedirectGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.hasRole(['ADMIN'])) {
    return router.createUrlTree(['/admin/revenue']);
  }

  if (authService.hasRole(['DOCTOR'])) {
    return router.createUrlTree(['/admin/appointments']);
  }

  if (authService.hasRole(['RECEPTIONIST'])) {
    return router.createUrlTree(['/admin/appointments']);
  }

  return router.createUrlTree(['/login']);
};
