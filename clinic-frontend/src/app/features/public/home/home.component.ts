import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';

// PrimeNG
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';

import { ChatbotComponent } from '../../../shared/components/chatbot/chatbot.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterLink, ButtonModule, DialogModule, ChatbotComponent],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent {

  private router = inject(Router);

  // Biến kiểm soát hiển thị Popup Đặt lịch
  showMethodDialog: boolean = false;

  // --- ACTIONS ---

  // Bấm nút "Đặt lịch ngay" -> Mở Popup
  onBookNowClick() {
    this.showMethodDialog = true;
  }

  // Điều hướng sang trang Booking
  navigateToBooking(method: 'CLINIC' | 'HOME') {
    this.showMethodDialog = false; // Đóng popup
    this.router.navigate(['/booking'], {
      queryParams: { method: method }
    });
  }
}
