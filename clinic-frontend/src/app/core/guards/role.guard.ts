import { inject } from '@angular/core';
import { Router, CanActivateFn } from '@angular/router';
import { AuthService } from '../services/auth.service'; // Sửa lại đường dẫn import cho đúng

export const RoleGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const authService = inject(AuthService);

  // Lấy danh sách role được phép truy cập từ cấu hình router
  const expectedRoles = route.data['roles'] as string[];

  // Nếu không yêu cầu role cụ thể nào -> cho qua
  if (!expectedRoles || expectedRoles.length === 0) {
    return true;
  }

  // Kiểm tra role bằng hàm hasRole ta đã viết ở AuthService
  if (authService.hasRole(expectedRoles)) {
    return true;
  } else {
    // Nếu không có quyền -> đá về trang nào đó (ví dụ trang chủ admin hoặc 403)
    // Ở đây mình đá tạm về trang login hoặc trang dashboard chung
    alert('Bạn không có quyền truy cập chức năng này!');
    // router.navigate(['/admin/dashboard']);
    return false;
  }
};
