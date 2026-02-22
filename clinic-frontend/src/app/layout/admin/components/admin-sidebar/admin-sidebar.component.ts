import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { LayoutService } from '../../../../core/services/layout.service';
import { AuthService } from '../../../../api/auth.service';

// 1. Định nghĩa Interface cho nhóm
interface MenuSection {
  label: string;      // Tên nhóm (VD: Vận hành, Hệ thống)
  items: MenuItem[];  // Các menu con bên trong
  visible?: boolean;  // Cờ để ẩn hiện sau khi lọc quyền
}

interface MenuItem {
  label: string;
  icon: string;
  routerLink?: string;
  isOpen?: boolean;
  roles?: string[];
  children?: MenuItem[];
}

@Component({
  selector: 'app-admin-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-sidebar.component.html',
  styleUrls: ['./admin-sidebar.component.scss']
})
export class AdminSidebarComponent implements OnInit {
  layoutService = inject(LayoutService);
  router = inject(Router);
  authService = inject(AuthService);

  // 2. Cấu trúc lại dữ liệu thành các Nhóm (Sections)
  rawMenuSections: MenuSection[] = [
    {
      label: 'Tổng quan',
      items: [
        { label: 'Thống kê doanh thu', icon: 'pi pi-chart-line', routerLink: '/admin/revenue', roles: ['ADMIN'] }
      ]
    },
    {
      label: 'Vận hành phòng khám',
      items: [
        { label: 'Lịch hẹn khám', icon: 'pi pi-calendar-clock', routerLink: '/admin/appointments', roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST'] },
        { label: 'Hồ sơ bệnh nhân', icon: 'pi pi-users', routerLink: '/admin/patients', roles: ['ADMIN', 'RECEPTIONIST'] },
        { label: 'Lịch làm việc', icon: 'pi pi-calendar', routerLink: '/admin/schedule', roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST'] },
      ]
    },
    {
      label: 'Quản lý chuyên môn',
      items: [
        { label: 'Bệnh án điện tử', icon: 'pi pi-folder-open', routerLink: '/admin/records', roles: ['ADMIN', 'DOCTOR'] },
        { label: 'Kê đơn thuốc', icon: 'pi pi-file-edit', routerLink: '/admin/prescriptions', roles: ['ADMIN', 'DOCTOR'] },
        { label: 'Kho dược', icon: 'pi pi-box', routerLink: '/admin/drugs', roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST'] },
      ]
    },
    {
      label: 'Tài chính & Dịch vụ',
      items: [
        { label: 'Quản lý hóa đơn', icon: 'pi pi-receipt', routerLink: '/admin/invoices', roles: ['ADMIN', 'RECEPTIONIST'] },
        { label: 'Danh mục dịch vụ', icon: 'pi pi-briefcase', routerLink: '/admin/services', roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST'] },
      ]
    },
    {
      label: 'Hệ thống',
      items: [
        {
          label: 'Nhân sự',
          icon: 'pi pi-id-card',
          isOpen: false,
          roles: ['ADMIN'],
          children: [
            { label: 'Bác sĩ', icon: 'pi pi-user-plus', routerLink: '/admin/staff/doctors', roles: ['ADMIN'] },
            { label: 'Lễ tân', icon: 'pi pi-calendar-plus', routerLink: '/admin/staff/receptionists', roles: ['ADMIN'] }
          ]
        },
        { label: 'Tài khoản người dùng', icon: 'pi pi-user-edit', routerLink: '/admin/users', roles: ['ADMIN'] },
        { label: 'Chuyên khoa', icon: 'pi pi-star', routerLink: '/admin/specialties', roles: ['ADMIN', 'RECEPTIONIST'] },
      ]
    }
  ];

  menuSections: MenuSection[] = [];

  ngOnInit() {
    this.authService.currentUser$.subscribe(() => this.updateMenu());
    this.updateMenu();
  }

  updateMenu() {
    // 3. Logic lọc quyền mới: Lọc từ Nhóm -> Menu con
    this.menuSections = this.rawMenuSections.map(section => {
      // Lọc các item trong section
      const filteredItems = section.items.filter(item => this.checkItemAccess(item));

      // Trả về section mới với items đã lọc
      return {
        ...section,
        items: filteredItems,
        // Section chỉ hiện nếu có ít nhất 1 item bên trong
        visible: filteredItems.length > 0
      };
    }).filter(section => section.visible); // Bỏ các section rỗng
  }

  // Hàm đệ quy kiểm tra quyền
  checkItemAccess(item: MenuItem): boolean {
    // Check role của item hiện tại
    if (item.roles && !this.authService.hasRole(item.roles)) {
      return false;
    }

    // Nếu có children, lọc children
    if (item.children) {
      item.children = item.children.filter(child => this.checkItemAccess(child));
      // Nếu lọc xong mà không còn con nào thì ẩn luôn cha (optional)
      // if (item.children.length === 0) return false;
    }
    return true;
  }

  toggleSubMenu(item: any) {
    if (this.layoutService.sidebarOpen()) {
      if (!item.isOpen) {
        // Đóng tất cả các menu con khác trong TẤT CẢ các section
        this.menuSections.forEach(section => {
          section.items.forEach(menu => {
            if (menu !== item && menu.children) menu.isOpen = false;
          });
        });
      }
      item.isOpen = !item.isOpen;
    }
  }

  isParentActive(item: any): boolean {
    if (!item.children) return false;
    return item.children.some((child: any) =>
      this.router.isActive(child.routerLink, {
        paths: 'subset', queryParams: 'ignored', fragment: 'ignored', matrixParams: 'ignored'
      })
    );
  }
}
