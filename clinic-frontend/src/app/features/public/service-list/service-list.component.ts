import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MasterDataService } from '../../../core/services/master-data.service';
import { ServiceEntityResponse } from '../../../models/master-data.model';

@Component({
  selector: 'app-service-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './service-list.component.html',
  styleUrls: ['./service-list.component.scss']
})
export class ServiceListComponent implements OnInit {
  originalServices: ServiceEntityResponse[] = [];
  displayServices: ServiceEntityResponse[] = [];

  searchTerm: string = '';
  selectedCategory: string = 'ALL';

  // Biến điều khiển dropdown
  showDropdown: boolean = false;

  constructor(
    private masterDataService: MasterDataService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.masterDataService.getAllServices(1, 100, true).subscribe({
      next: (res) => {
        this.originalServices = res.result?.data || [];
        this.filterServices();
      },
      error: (err) => console.error('Lỗi tải dịch vụ:', err)
    });
  }

  filterServices() {
    let temp = this.originalServices;

    if (this.selectedCategory !== 'ALL') {
      temp = temp.filter(s => s.type === this.selectedCategory);
    }

    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase().trim();
      temp = temp.filter(s => s.name.toLowerCase().includes(term));
    }

    this.displayServices = temp;
  }

  // SỬA: Hàm chọn category và đóng dropdown
  selectCategory(type: string) {
    this.selectedCategory = type;
    this.showDropdown = false; // Đóng menu sau khi chọn
    this.filterServices();
  }

  // MỚI: Bật tắt dropdown
  toggleDropdown() {
    this.showDropdown = !this.showDropdown;
  }

  // MỚI: Lấy tên hiển thị cho dropdown
  getCategoryLabel(type: string): string {
    switch(type) {
      case 'ALL': return 'Tất cả dịch vụ';
      case 'CONSULTATION': return 'Khám bệnh';
      case 'PARACLINICAL': return 'Cận lâm sàng';
      default: return 'Tất cả';
    }
  }

  navigateToBooking(service: ServiceEntityResponse) {
    this.router.navigate(['/booking'], {
      queryParams: {
        serviceId: service.serviceId,
        mode: 'TIME'
      }
    });
  }

  getServiceImage(service: any): string {
    if (service.image) return service.image;
    return 'https://img.freepik.com/free-vector/doctor-character-background_1270-84.jpg';
  }

  scrollDown() {
    document.getElementById('search-area')?.scrollIntoView({ behavior: 'smooth' });
  }
}
