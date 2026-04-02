import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { StaffService } from '../../../api/staff.service';
import { MasterDataService } from '../../../api/master-data.service';
import { DoctorResponse } from '../../../models/staff.model';
import { SpecialtyResponse } from '../../../models/master-data.model';

@Component({
  selector: 'app-doctor-list',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './doctor-list.component.html',
  styleUrls: ['./doctor-list.component.scss']
})
export class DoctorListComponent implements OnInit {
  doctors: DoctorResponse[] = [];
  specialties: SpecialtyResponse[] = [];

  // Filter States
  keyword: string = '';
  selectedSpecialtyId: number | null = null;
  selectedSpecialtyName: string = 'Tất cả chuyên khoa';

  // UI Control
  showDropdown: boolean = false;

  page: number = 1;
  size: number = 100;

  private staffService = inject(StaffService);
  private masterDataService = inject(MasterDataService);

  ngOnInit(): void {
    this.fetchDoctors();
    this.fetchSpecialties();
  }

  fetchDoctors() {
    // Gọi API lấy tất cả, sau đó lọc Client-side (do API search hiện tại có thể chưa support full filter)
    this.staffService.getAllDoctors(this.page, this.size, this.keyword).subscribe({
      next: (res) => {
        let data = res.result?.data || [];

        // Lọc theo chuyên khoa nếu có chọn
        if (this.selectedSpecialtyId) {
             data = data.filter(d => d.specialtyId === this.selectedSpecialtyId);
        }
        this.doctors = data;
      },
      error: (err) => console.error('Lỗi tải bác sĩ:', err)
    });
  }

  fetchSpecialties() {
    this.masterDataService.getAllSpecialties(1, 100).subscribe({
      next: (res) => {
        this.specialties = res.result?.data || [];
      }
    });
  }

  // --- ACTIONS ---
  onSearch() {
    this.page = 1;
    this.fetchDoctors();
  }

  toggleDropdown() {
    this.showDropdown = !this.showDropdown;
  }

  selectSpecialty(spec: SpecialtyResponse | null) {
    if (spec) {
      this.selectedSpecialtyId = spec.specialtyId;
      this.selectedSpecialtyName = spec.name;
    } else {
      this.selectedSpecialtyId = null;
      this.selectedSpecialtyName = 'Tất cả chuyên khoa';
    }

    this.showDropdown = false; // Đóng menu
    this.onSearch(); // Gọi tìm kiếm lại ngay
  }

  getSpecialtyLabel(): string {
    return this.selectedSpecialtyName;
  }

  resetSearch() {
    this.keyword = '';
    this.selectSpecialty(null);
  }

  scrollDown() {
    document.getElementById('search-area')?.scrollIntoView({ behavior: 'smooth' });
  }

  // Helper hiển thị ảnh
  getDoctorImage(doctor: any): string {
     if (doctor && doctor.image) return doctor.image;
     return doctor.gender === 'FEMALE'
        ? 'https://img.freepik.com/free-vector/doctor-character-background_1270-84.jpg'
        : 'https://img.freepik.com/free-vector/doctor-character-background_1270-84.jpg';
  }
}
