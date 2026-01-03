import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router'; // QUAN TRỌNG: Để hiển thị nội dung trang con
import { HeaderComponent } from '../components/header/header.component'; // Import Header
import { FooterComponent } from '../components/footer/footer.component'; // Import Footer

@Component({
  selector: 'app-public-layout',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet,     // Bắt buộc có
    HeaderComponent,  // Khai báo Header
    FooterComponent   // Khai báo Footer
  ],
  templateUrl: './public-layout.component.html',
  styleUrls: ['./public-layout.component.scss']
})
export class PublicLayoutComponent {}
