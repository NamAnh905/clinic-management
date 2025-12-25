import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

// LƯU Ý: Thêm "../" vào đường dẫn import vì file đã vào sâu hơn 1 cấp
import { AdminSidebarComponent } from '../components/admin-sidebar/admin-sidebar.component';
import { AdminHeaderComponent } from '../components/admin-header/admin-header.component';
import { LayoutService } from '../../../core/services/layout.service';

@Component({
  selector: 'app-admin-layout',
  standalone: true,
  imports: [CommonModule, RouterModule, AdminSidebarComponent, AdminHeaderComponent],
  templateUrl: './admin-layout.component.html',
  styleUrls: ['./admin-layout.component.scss']
})
export class AdminLayoutComponent {
  layoutService = inject(LayoutService);
}
