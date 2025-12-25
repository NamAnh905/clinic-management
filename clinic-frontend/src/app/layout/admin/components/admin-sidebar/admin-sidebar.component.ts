import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { LayoutService } from '../../../../core/services/layout.service';
import { AuthService } from '../../../../core/services/auth.service';

interface MenuItem {
  label: string;
  icon: string;
  routerLink?: string;
  isOpen?: boolean;
  roles?: string[]; // Mảng các role ĐƯỢC PHÉP nhìn thấy
  children?: MenuItem[];
}

@Component({
  selector: 'app-admin-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-sidebar.component.html', // Giữ nguyên file HTML cũ
  styleUrls: ['./admin-sidebar.component.scss']   // Giữ nguyên file SCSS cũ
})
export class AdminSidebarComponent implements OnInit {
  layoutService = inject(LayoutService);
  router = inject(Router);
  authService = inject(AuthService);

  // Danh sách menu gốc với cấu hình Role
  allMenuItems: MenuItem[] = [
    // --- NHÓM 1: TIẾP ĐÓN & ĐIỀU PHỐI (Dùng nhiều nhất) ---
    {
      label: 'Lịch hẹn',
      icon: 'pi pi-stopwatch',
      routerLink: '/admin/appointments',
      roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST']
    },
    {
      label: 'Quản lý bệnh nhân',
      icon: 'pi pi-shield',
      routerLink: '/admin/patients',
      roles: ['ADMIN', 'RECEPTIONIST']
    },
    {
      label: 'Lịch làm việc',
      icon: 'pi pi-calendar',
      routerLink: '/admin/schedule',
      roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST']
    },

    // --- NHÓM 2: LÂM SÀNG (Dành cho Bác sĩ) ---
    {
      label: 'Hồ sơ bệnh án',
      icon: 'pi pi-folder-open',
      routerLink: '/admin/records',
      roles: ['ADMIN', 'DOCTOR']
    },
    {
      label: 'Quản lý đơn thuốc',
      icon: 'pi pi-file-edit',
      routerLink: '/admin/prescriptions',
      roles: ['ADMIN', 'DOCTOR']
    },

    // --- NHÓM 3: TÀI CHÍNH & DƯỢC (Hậu cần) ---
    {
      label: 'Quản lý hóa đơn',
      icon: 'pi pi-receipt',
      routerLink: '/admin/invoices',
      roles: ['ADMIN', 'RECEPTIONIST']
    },
    {
      label: 'Quản lý thuốc',
      icon: 'pi pi-filter',
      routerLink: '/admin/drugs',
      roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST']
    },
    {
      label: 'Dịch vụ khám',
      icon: 'pi pi-briefcase',
      routerLink: '/admin/services',
      roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST']
    },

    // --- NHÓM 4: QUẢN TRỊ HỆ THỐNG (Chỉ Admin hoặc Setup ban đầu) ---
    {
      label: 'Quản lý nhân viên',
      icon: 'pi pi-id-card',
      isOpen: false,
      roles: ['ADMIN'],
      children: [
        { label: 'Bác sĩ', icon: 'pi pi-user-plus', routerLink: '/admin/staff/doctors', roles: ['ADMIN'] },
        { label: 'Lễ tân', icon: 'pi pi-calendar-plus', routerLink: '/admin/staff/receptionists', roles: ['ADMIN'] }
      ]
    },
    {
      label: 'Quản lý người dùng',
      icon: 'pi pi-users',
      routerLink: '/admin/users',
      roles: ['ADMIN']
    },
    {
      label: 'Quản lý chuyên khoa',
      icon: 'pi pi-star',
      routerLink: '/admin/specialties',
      roles: ['ADMIN', 'RECEPTIONIST']
    },
  ];

  menuItems: MenuItem[] = [];

  ngOnInit() {
    // Subscribe để khi user login/F5 load lại trang thì menu tự cập nhật
    this.authService.currentUser$.subscribe(user => {
      this.updateMenu();
    });

    // Gọi lần đầu (đề phòng trường hợp F5 data load từ localStorage)
    this.updateMenu();
  }

  updateMenu() {
    // Hàm lọc menu đệ quy
    this.menuItems = this.filterMenuByRole(this.allMenuItems);
  }

  filterMenuByRole(items: MenuItem[]): MenuItem[] {
    return items.filter(item => {
      // 1. Kiểm tra role
      // Nếu item có quy định role, thì check xem user hiện tại có role đó không
      if (item.roles && !this.authService.hasRole(item.roles)) {
        return false; // Ẩn nếu không có quyền
      }

      // 2. Xử lý menu con (recursive)
      if (item.children) {
        // Copy mảng children để không ảnh hưởng mảng gốc
        const filteredChildren = this.filterMenuByRole([...item.children]);

        // Gán lại children đã lọc
        item.children = filteredChildren;

        // (Tuỳ chọn) Nếu lọc xong mà không còn con nào thì ẩn luôn cha?
        // if (filteredChildren.length === 0) return false;
      }

      return true;
    });
  }

  toggleSubMenu(item: any) {
    if (this.layoutService.sidebarOpen()) {
      if (!item.isOpen) {
        this.menuItems.forEach(menu => {
          if (menu !== item && menu.children) {
            menu.isOpen = false;
          }
        });
      }
      item.isOpen = !item.isOpen;
    }
  }

  isParentActive(item: any): boolean {
    if (!item.children) return false;
    return item.children.some((child: any) =>
      this.router.isActive(child.routerLink, {
        paths: 'subset',
        queryParams: 'ignored',
        fragment: 'ignored',
        matrixParams: 'ignored'
      })
    );
  }
}
