import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { StaffService } from '../../../core/services/staff.service';
import { MasterDataService } from '../../../core/services/master-data.service';
import { DoctorResponse } from '../../../models/staff.model';
import { SpecialtyResponse } from '../../../models/master-data.model';

@Component({
  selector: 'app-doctor-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './doctor-list.component.html',
  styleUrls: ['./doctor-list.component.scss']
})
export class DoctorListComponent implements OnInit {
  doctors: DoctorResponse[] = [];
  specialties: SpecialtyResponse[] = [];

  keyword: string = '';
  selectedSpecialtyId: string = '';

  page: number = 1;
  size: number = 100;

  private staffService = inject(StaffService);
  private masterDataService = inject(MasterDataService);

  ngOnInit(): void {
    this.fetchDoctors();
    this.fetchSpecialties();
  }

  fetchDoctors() {
    this.staffService.getAllDoctors(this.page, this.size, this.keyword).subscribe({
      next: (res) => {
        let data = res.result?.data || [];

        // Filter Client-side theo chuyên khoa (Nếu API chưa hỗ trợ filter này)
        if (this.selectedSpecialtyId) {
             const specId = Number(this.selectedSpecialtyId);
             data = data.filter(d => d.specialtyId === specId);
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

  onSearch() {
    this.page = 1;
    this.fetchDoctors();
  }

  getDoctorImage(doctor: any): string {
     if (doctor && doctor.image) return doctor.image;
     return doctor.gender === 'FEMALE'
        ? 'https://img.freepik.com/free-photo/pleased-young-female-doctor-wearing-medical-robe-stethoscope-around-neck-standing-closed-posture_409827-254.jpg'
        : 'https://img.freepik.com/free-photo/portrait-smiling-handsome-male-doctor-man_171337-5055.jpg';
  }

  scrollDown() {
    document.getElementById('main-content')?.scrollIntoView({ behavior: 'smooth' });
  }
}
