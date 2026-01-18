import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';

// PrimeNG Modules
import { AvatarModule } from 'primeng/avatar';
import { MenuModule } from 'primeng/menu';
import { MenuItem, MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

// Services
import { UserService } from '../../../../core/services/user.service';
import { AuthService } from '../../../../core/services/auth.service';
import { UserResponse } from '../../../../models/user.model';

// Child Components
import { ProfileUpdateDialogComponent } from '../../../../shared/components/profile-update-dialog/profile-update-dialog.component';
import { AppointmentHistoryDialogComponent } from '../../../../shared/components/appointment-history-dialog/appointment-history-dialog.component';
import { ChangePasswordDialogComponent } from '../../../../shared/components/change-password-dialog/change-password-dialog.component';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [
    CommonModule, RouterLink, RouterLinkActive,
    AvatarModule, MenuModule, ToastModule,
    ProfileUpdateDialogComponent,
    AppointmentHistoryDialogComponent,
    ChangePasswordDialogComponent
  ],
  providers: [MessageService],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {
  currentUser: UserResponse | null = null;
  userMenuItems: MenuItem[] = [];

  // Dialog Control Flags
  showProfileDialog = false;
  showHistoryDialog = false;
  showPasswordDialog = false;

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
        command: () => this.showProfileDialog = true
      },
      {
        label: 'Lịch sử đặt khám',
        icon: 'pi pi-calendar',
        command: () => this.showHistoryDialog = true
      },
      {
        label: 'Đổi mật khẩu',
        icon: 'pi pi-key',
        command: () => this.showPasswordDialog = true
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

  handleUserUpdate(updatedUser: UserResponse) {
    this.currentUser = updatedUser; // Cập nhật lại UI ngay lập tức
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
