import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

// PrimeNG Modules
import { AvatarModule } from 'primeng/avatar';
import { MenuModule } from 'primeng/menu';
import { MenuItem, MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

// Services
import { UserService } from '../../../../api/user.service';
import { AuthService } from '../../../../api/auth.service';
import { UserResponse } from '../../../../models/user.model';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule, RouterLink, RouterLinkActive,
    AvatarModule, MenuModule, ToastModule
  ],
  providers: [MessageService],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
  currentUser: UserResponse | null = null;
  userMenuItems: MenuItem[] = [];

  private userService = inject(UserService);
  private authService = inject(AuthService);
  private router = inject(Router);

  ngOnInit() {
    this.checkLoginStatus();
  }

  checkLoginStatus() {
    const token = localStorage.getItem('token') || localStorage.getItem('access_token');
    if (!token) {
        this.currentUser = null;
        return;
    }
    this.userService.getMyInfo().subscribe({
      next: (res) => {
        if (res.result) {
          this.currentUser = res.result;
          this.initUserMenu();
        }
      },
      error: () => {
        this.currentUser = null;
        localStorage.removeItem('token');
      }
    });
  }

  initUserMenu() {
    this.userMenuItems = [
      {
        label: 'Hồ sơ cá nhân',
        icon: 'pi pi-user',
        routerLink: '/profile/account' // Dùng routerLink thay vì command
      },
      {
        label: 'Lịch sử đặt khám',
        icon: 'pi pi-calendar',
        routerLink: '/profile/appointments'
      },
      {
        label: 'Đơn thuốc',
        icon: 'pi pi-file',
        routerLink: '/profile/prescriptions'
      },
      {
        label: 'Lịch sử thanh toán',
        icon: 'pi pi-wallet',
        routerLink: '/profile/invoices'
      },
      {
        label: 'Đổi mật khẩu',
        icon: 'pi pi-key',
        routerLink: '/profile/password'
      },
      { separator: true },
      {
        label: 'Đăng xuất',
        icon: 'pi pi-sign-out',
        styleClass: 'text-red-500',
        command: () => this.logout()
      }
    ];
  }

  logout() {
    this.authService.logout().subscribe({
        next: () => console.log('Logout server success'),
        error: (err) => console.warn('Logout server failed', err)
    });
    localStorage.removeItem('token');
    this.currentUser = null;
    this.router.navigate(['/login']);
  }
}
