import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LayoutService {
  // Signal quản lý trạng thái: true = Đang mở, false = Đang thu nhỏ
  sidebarOpen = signal<boolean>(true);

  toggleSidebar() {
    this.sidebarOpen.update(state => !state);
  }
}
