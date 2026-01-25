import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';

// PrimeNG
import { TableModule } from 'primeng/table';
import { ButtonModule } from 'primeng/button';
import { DialogModule } from 'primeng/dialog';
import { TagModule } from 'primeng/tag';
import { MessageService } from 'primeng/api';
import { ToastModule } from 'primeng/toast';

// Services & Models
import { MedicalService } from '../../../api/medical.service';
import { MedicalRecordResponse, PrescriptionResponse } from '../../../models/medical.model';

@Component({
  selector: 'app-user-prescription',
  standalone: true,
  imports: [CommonModule, TableModule, ButtonModule, DialogModule, TagModule, ToastModule],
  providers: [MessageService],
  templateUrl: './user-prescription.component.html',
  styleUrls: ['./user-prescription.component.scss']
})
export class UserPrescriptionComponent implements OnInit {
  records: MedicalRecordResponse[] = [];
  loading: boolean = false;

  // Pagination
  page: number = 0;
  size: number = 5;
  totalRecords: number = 0;

  // Dialog Đơn thuốc
  showPrescriptionDialog: boolean = false;
  selectedPrescription: PrescriptionResponse | null = null;
  loadingPrescription: boolean = false;

  private medicalService = inject(MedicalService);
  private messageService = inject(MessageService);

  ngOnInit() {
    this.loadMedicalRecords();
  }

  loadMedicalRecords() {
    // 1. LOGIC CACHE: Chỉ áp dụng khi đang ở trang đầu tiên (page = 0)
    const isFirstPage = this.page === 0;

    if (isFirstPage && this.medicalService.medicalRecordsCache) {
      // Có cache -> Hiển thị ngay lập tức
      this.records = this.medicalService.medicalRecordsCache;
      this.loading = false; // Không hiện loading spinner
    } else {
      // Không có cache hoặc đang chuyển trang -> Hiện loading như bình thường
      this.loading = true;
    }

    // 2. GỌI API (Silent Update nếu đã có cache)
    this.medicalService.getMedicalRecords(
      this.page + 1,
      this.size,
      undefined, undefined, undefined, undefined,
      'visitDate', 'desc'
    ).subscribe({
      next: (res) => {
        if (res.result) {
          const newData = (res.result as any).data || [];
          this.records = newData;
          this.totalRecords = res.result.totalElements;

          // 3. CẬP NHẬT CACHE: Nếu đang load trang 1 thành công, lưu lại vào Service
          if (isFirstPage) {
            this.medicalService.medicalRecordsCache = newData;
          }
        }
        this.loading = false;
      },
      error: (err) => {
        console.error(err);
        this.loading = false;

        // Chỉ báo lỗi cho người dùng nếu danh sách đang trống (lần đầu load thất bại)
        // Nếu đã hiện cache cũ rồi thì lờ đi, tránh làm phiền
        if (this.records.length === 0) {
           this.messageService.add({severity: 'error', summary: 'Lỗi', detail: 'Không tải được hồ sơ bệnh án.'});
        }
      }
    });
  }

  viewPrescription(record: MedicalRecordResponse) {
    this.loadingPrescription = true;
    this.selectedPrescription = null;

    // BƯỚC 1: Lấy thông tin Đơn thuốc từ Hồ sơ bệnh án
    this.medicalService.getPrescriptionByRecord(record.recordId).subscribe({
      next: (res) => {
        if (res.result) {
          const prescription = res.result;

          // BƯỚC 2: Gọi tiếp API lấy chi tiết thuốc (Danh sách thuốc)
          this.medicalService.getPrescriptionDetails(prescription.prescriptionId).subscribe({
            next: (detailRes) => {
              // Gán danh sách thuốc vào đối tượng đơn thuốc
              prescription.prescriptionDetails = detailRes.result || [];

              this.selectedPrescription = prescription;
              this.showPrescriptionDialog = true;
              this.loadingPrescription = false;
            },
            error: (err) => {
              console.error('Lỗi lấy chi tiết thuốc', err);
              // Vẫn hiện đơn thuốc dù lỗi lấy thuốc (để xem ghi chú/bác sĩ)
              prescription.prescriptionDetails = [];
              this.selectedPrescription = prescription;
              this.showPrescriptionDialog = true;
              this.loadingPrescription = false;
            }
          });

        } else {
          this.messageService.add({ severity: 'info', summary: 'Thông báo', detail: 'Lần khám này không có đơn thuốc.' });
          this.loadingPrescription = false;
        }
      },
      error: (err) => {
        // Lỗi 404 thường nghĩa là chưa có đơn thuốc
        this.messageService.add({ severity: 'info', summary: 'Thông báo', detail: 'Chưa có đơn thuốc cho hồ sơ này.' });
        this.loadingPrescription = false;
      }
    });
  }

  onPageChange(event: any) {
    this.page = event.first / event.rows;
    this.size = event.rows;
    this.loadMedicalRecords();
  }
}
