import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MasterDataService } from '../../../core/services/master-data.service';
import { ServiceEntityResponse } from '../../../models/master-data.model';

@Component({
  selector: 'app-service-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './service-list.component.html',
  styleUrls: ['./service-list.component.scss']
})
export class ServiceListComponent implements OnInit {
  services: ServiceEntityResponse[] = [];

  constructor(private masterDataService: MasterDataService) {}

  ngOnInit(): void {
    // Lấy tất cả dịch vụ đang hoạt động (activeOnly = true)
    this.masterDataService.getAllServices(1, 100, true).subscribe({
      next: (res) => {
        this.services = res.result?.data || [];
      },
      error: (err) => console.error('Lỗi tải dịch vụ:', err)
    });
  }

  // Hàm lấy ảnh, nếu null thì trả về ảnh mặc định
  getServiceImage(service: any): string {
    if (service.image) return service.image;
    // Ảnh mặc định icon y tế nếu chưa up ảnh
    return 'https://img.freepik.com/free-vector/doctor-character-background_1270-84.jpg';
  }

  scrollDown() {
    document.getElementById('main-content')?.scrollIntoView({ behavior: 'smooth' });
  }
}
