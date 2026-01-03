import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms'; // Để dùng ngModel cho ô tìm kiếm
import { RouterLink } from '@angular/router';
import { MasterDataService } from '../../../core/services/master-data.service';
import { SpecialtyResponse } from '../../../models/master-data.model';

@Component({
  selector: 'app-specialty-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './specialty-list.component.html',
  styleUrls: ['./specialty-list.component.scss']
})
export class SpecialtyListComponent implements OnInit {

  specialties: SpecialtyResponse[] = [];
  keyword: string = '';
  page: number = 1;
  size: number = 100; // Lấy nhiều để hiện hết một lượt

  // Map icon cho đẹp (Vì DB chưa có trường icon/image)
  iconMap: any = {
    'Nội khoa': 'fa-heart-pulse',
    'Ngoại khoa': 'fa-scalpel',
    'Nhi khoa': 'fa-baby',
    'Sản phụ khoa': 'fa-person-pregnant',
    'Da liễu': 'fa-spa',
    'Mắt': 'fa-eye',
    'Tai Mũi Họng': 'fa-ear-listen',
    'Răng Hàm Mặt': 'fa-tooth',
    'Xét nghiệm': 'fa-flask',
    'Chẩn đoán hình ảnh': 'fa-x-ray'
  };

  constructor(private masterDataService: MasterDataService) {}

  ngOnInit(): void {
    this.loadSpecialties();
  }

  loadSpecialties() {
    this.masterDataService.getAllSpecialties(this.page, this.size, this.keyword).subscribe({
      next: (res) => {
        this.specialties = res.result?.data;
      },
      error: (err) => console.error('Lỗi tải chuyên khoa:', err)
    });
  }

  onSearch() {
    this.page = 1;
    this.loadSpecialties();
  }

  // Helper lấy icon
  getIcon(name: string): string {
    // Tìm icon theo tên, nếu không có thì lấy icon bác sĩ mặc định
    return this.iconMap[name] || 'fa-user-doctor';
  }

  scrollDown() {
    document.getElementById('main-content')?.scrollIntoView({ behavior: 'smooth' });
  }
}
