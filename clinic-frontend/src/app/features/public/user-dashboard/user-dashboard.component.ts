import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../api/auth.service';

@Component({
  selector: 'app-user-dashboard',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './user-dashboard.component.html',
  styleUrls: ['./user-dashboard.component.scss']
})
export class UserDashboardComponent {

  private authService = inject(AuthService);
  private router = inject(Router);

  menuItems = [
    { label: 'Hồ sơ cá nhân', icon: 'pi pi-user', route: '/profile/account' },
    { label: 'Lịch sử đặt khám', icon: 'pi pi-calendar', route: '/profile/appointments' },
    { label: 'Đơn thuốc', icon: 'pi pi-file', route: '/profile/prescriptions' },
    { label: 'Lịch sử thanh toán', icon: 'pi pi-wallet', route: '/profile/invoices' },
    { label: 'Đổi mật khẩu', icon: 'pi pi-key', route: '/profile/password' }
  ];

  logout() {
    this.authService.logout().subscribe({
      next: () => console.log('Logout success'),
      error: (err) => console.warn('Logout failed', err)
    });
    localStorage.removeItem('token');
    // Xóa thêm các key khác nếu có (ví dụ: currentUser)
    this.router.navigate(['/login']);
  }
}
