import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { MasterDataService } from '../../../api/master-data.service';
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
  size: number = 100;

  // Map icon FontAwesome (Có thể bổ sung thêm nếu cần)
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
    'Chẩn đoán hình ảnh': 'fa-x-ray',
    'Cơ Xương Khớp': 'fa-bone',
    'Thần kinh': 'fa-brain',
    'Tiêu hóa': 'fa-utensils' // Hoặc icon khác phù hợp hơn
  };

  constructor(private masterDataService: MasterDataService) {}

  ngOnInit(): void {
    this.loadSpecialties();
  }

  loadSpecialties() {
    this.masterDataService.getAllSpecialties(this.page, this.size, this.keyword).subscribe({
      next: (res) => {
        this.specialties = res.result?.data || [];
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
    // Logic: Kiểm tra xem tên chuyên khoa có chứa từ khóa trong map không
    // Ví dụ: "Chuyên khoa Nhi" -> chứa "Nhi" -> trả về 'fa-baby'
    for (const key in this.iconMap) {
        if (name.toLowerCase().includes(key.toLowerCase())) {
            return this.iconMap[key];
        }
    }
    return 'fa-user-doctor'; // Icon mặc định
  }

  scrollDown() {
    document.getElementById('search-area')?.scrollIntoView({ behavior: 'smooth' });
  }
}
