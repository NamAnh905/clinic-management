import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';

// PrimeNG Imports
import { ButtonModule } from 'primeng/button';
import { CalendarModule } from 'primeng/calendar';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { MessageService } from 'primeng/api';

// Services
import { MasterDataService } from '../../../core/services/master-data.service';
import { StaffService } from '../../../core/services/staff.service';
import { AppointmentService } from '../../../core/services/appointment.service';
import { UserService } from '../../../core/services/user.service';

@Component({
  selector: 'app-booking',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule,
    ButtonModule, CalendarModule, DialogModule, ToastModule,
    InputTextModule, InputTextareaModule
  ],
  providers: [MessageService],
  templateUrl: './booking.component.html',
  styleUrls: ['./booking.component.scss']
})
export class BookingComponent implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private masterDataService = inject(MasterDataService);
  private staffService = inject(StaffService);
  private appointmentService = inject(AppointmentService);
  private userService = inject(UserService);
  private fb = inject(FormBuilder);
  private messageService = inject(MessageService);

  // --- STATE ---
  viewMode: 'CLINIC' | 'SERVICE' | 'DOCTOR_LIST' = 'CLINIC';
  progress: number = 25;

  // Dialog State
  showModeDialog: boolean = false;
  showTimeDialog: boolean = false;
  showInfoDialog: boolean = false;

  // Data
  services: any[] = [];
  doctors: any[] = [];
  availableSlots: string[] = [];

  // Selection
  selectedService: any = null;
  selectedDoctor: any = null;
  selectedDate: Date = new Date();
  selectedTime: string = '';
  bookingMode: 'TIME' | 'DOCTOR' = 'DOCTOR';

  infoForm: FormGroup;
  currentUser: any = null;
  currentDate = new Date();

  constructor() {
    this.infoForm = this.fb.group({
      fullName: ['', Validators.required],
      phoneNumber: ['', [Validators.required, Validators.pattern(/(84|0[3|5|7|8|9])+([0-9]{8})\b/)]],
      email: [''],
      dateOfBirth: [null],
      gender: ['MALE'],
      reason: ['', Validators.required],
      address: ['']
    });
  }

  ngOnInit() {
    this.loadServices();

    // 1. Tự động điền thông tin nếu đã đăng nhập
    this.checkUserLogin();

    this.route.queryParams.subscribe(params => this.syncStateWithUrl(params));
  }

  // --- LOGIC SYNC URL ---
  syncStateWithUrl(params: any) {
    this.showModeDialog = false;
    this.showTimeDialog = false;
    this.showInfoDialog = false;

    if (!params['facilityId']) {
      this.viewMode = 'CLINIC';
      this.progress = 25;
    }
    else if (!params['serviceId']) {
      this.viewMode = 'SERVICE';
      this.progress = 50;
    }
    else {
      if (this.services.length > 0 && !this.selectedService) {
        this.selectedService = this.services.find(s => s.serviceId == params['serviceId']);
      }

      const mode = params['mode'];
      if (mode === 'DOCTOR') {
        this.bookingMode = 'DOCTOR';
        this.viewMode = 'DOCTOR_LIST';
        this.progress = 75;
        if (this.doctors.length === 0) this.loadDoctors(params['serviceId']);

        if (params['doctorId']) {
             if(!this.selectedDoctor && this.doctors.length > 0) {
                this.selectedDoctor = this.doctors.find(d => d.doctorId == params['doctorId']);
             }
             this.progress = 85;
             this.showTimeDialog = true;
             this.onDateSelect();
        }
      } else if (mode === 'TIME') {
        this.bookingMode = 'TIME';
        this.viewMode = 'SERVICE';
        this.progress = 60;
        if (!params['time']) {
           this.showTimeDialog = true;
           if(this.availableSlots.length === 0) this.onDateSelect();
        }
      } else {
        this.viewMode = 'SERVICE';
        this.showModeDialog = true;
        this.progress = 55;
      }

      if (params['date'] && params['time']) {
          this.selectedDate = new Date(params['date']);
          this.selectedTime = params['time'];
          this.showTimeDialog = false;

          // 2. Mở Dialog Info để người dùng Review lại thông tin (đã được auto-fill)
          this.showInfoDialog = true;
          this.progress = 95;
      }
    }
  }

  updateParams(newParams: any) {
    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: newParams,
      queryParamsHandling: 'merge'
    });
  }

  // --- ACTIONS ---
  selectFacility() { this.updateParams({ facilityId: 1 }); }
  selectService(service: any) {
    this.selectedService = service;
    this.updateParams({ serviceId: service.serviceId, mode: null });

    // THÊM DÒNG NÀY: Mở ngay dialog Mode mà không cần chờ URL change
    this.showModeDialog = true;
  }
  confirmMode(mode: 'TIME' | 'DOCTOR') {
    this.bookingMode = mode;
    this.updateParams({ mode: mode });

    // THÊM LOGIC NÀY:
    if (mode === 'TIME') {
        // Nếu chọn theo giờ -> Mở ngay dialog chọn giờ
        this.showTimeDialog = true;
        // Gọi load slot ngay lập tức
        if (this.availableSlots.length === 0) this.onDateSelect();
    }
    // Nếu mode DOCTOR thì không cần làm gì, view sẽ tự đổi sang list bác sĩ
  }
  selectDoctor(doc: any) {
    this.selectedDoctor = doc;
    this.updateParams({ doctorId: doc.doctorId });

    // THÊM DÒNG NÀY: Mở ngay dialog chọn giờ
    this.showTimeDialog = true;
    this.onDateSelect(); // Load slot ngay
  }

  onDateSelect() {
    if(!this.selectedDate) return;
    const dateStr = this.formatDate(this.selectedDate);
    if (this.bookingMode === 'DOCTOR' && this.selectedDoctor) {
      this.getSlots(this.selectedDoctor.doctorId, dateStr);
    } else {
      this.findAvailableDoctorForService(dateStr);
    }
  }

  findAvailableDoctorForService(dateStr: string) {
      if (!this.selectedService) return;

      // 1. Lấy danh sách chuyên khoa để tìm ID chuyên khoa đúng
      this.masterDataService.getAllSpecialties(1, 100).subscribe(specRes => {
          const specialties = specRes.result?.data || [];

          // Tìm chuyên khoa tương ứng với dịch vụ đang chọn
          const targetSpec = specialties.find((s: any) => s.defaultServiceId == this.selectedService.serviceId);

          if (targetSpec) {
              // 2. Nếu tìm thấy chuyên khoa -> Lấy danh sách bác sĩ
              this.staffService.getAllDoctors(1, 100).subscribe(res => {
                  const docs = res.result?.data || [];

                  // Lọc ra các bác sĩ thuộc chuyên khoa này
                  const availableDocs = docs.filter((d: any) => d.specialtyId == targetSpec.specialtyId);

                  if (availableDocs.length > 0) {
                      // 3. Lấy đại diện bác sĩ đầu tiên để hiển thị Slot
                      // (Logic thực tế có thể phức tạp hơn: gộp lịch của tất cả bác sĩ)
                      // Ở đây ta lấy lịch của người đầu tiên tìm được
                      this.getSlots(availableDocs[0].doctorId, dateStr);
                  } else {
                      this.availableSlots = []; // Không có bác sĩ nào thuộc khoa này
                  }
              });
          } else {
              // Trường hợp dịch vụ không thuộc chuyên khoa nào (hoặc cấu hình sai)
              this.availableSlots = [];
          }
      });
  }

  getSlots(docId: number, dateStr: string) {
    this.appointmentService.getAvailableSlots(docId, dateStr).subscribe(res => {
      this.availableSlots = res.result || [];
    });
  }

  selectTime(time: string) {
    this.selectedTime = time;
    this.updateParams({ date: this.formatDate(this.selectedDate), time: time });
  }

  // --- LOGIC XÁC NHẬN ĐẶT LỊCH ---
  confirmBooking() {
    if (this.infoForm.invalid) {
        this.infoForm.markAllAsTouched();
        return;
    }
    this.progress = 100;

    // Logic auto-assign bác sĩ nếu chọn theo giờ
    let docId = this.selectedDoctor?.doctorId;
    if (this.bookingMode === 'TIME' && !docId) {
         this.staffService.getAllDoctors(1, 50).subscribe(res => {
             const docs = res.result?.data || [];
             const doc = docs.find((d:any) => d.specialtyId == this.selectedService.serviceId);
             if(doc) this.finalSubmit(doc.doctorId);
         });
    } else {
        this.finalSubmit(docId);
    }
  }

  finalSubmit(doctorId: number) { // doctorId truyền vào đây đang là ID Bảng Doctor (VD: 16)
    const p = this.infoForm.value;
    const dateTimeStr = `${this.formatDate(this.selectedDate)}T${this.selectedTime}:00`;

    const selectedDocObj = this.doctors.find(d => d.doctorId === doctorId);
    const realDoctorIdToSend = selectedDocObj ? (selectedDocObj.userId || selectedDocObj.user_id) : doctorId;

    if (!realDoctorIdToSend) {
        this.messageService.add({severity:'error', summary:'Lỗi', detail:'Không tìm thấy thông tin User ID của bác sĩ.'});
        return;
    }
    // ---------------------------

    // KIỂM TRA: Đã đăng nhập hay chưa?
    if (this.currentUser && this.currentUser.userId) {
        const myId = this.currentUser.userId || this.currentUser.id;

        const requestBody = {
            patientId: Number(myId),
            doctorId: Number(realDoctorIdToSend), // <--- Gửi UserID của bác sĩ (VD: 35) thay vì 16
            appointmentTime: dateTimeStr,
            reason: p.reason
        };

        this.appointmentService.bookAppointment(requestBody).subscribe({
            next: () => this.handleSuccess('admin'),
            error: (err) => this.handleError(err)
        });
    } else {
        // Khách vãng lai
        const requestBody = {
            ...p,
            dateOfBirth: p.dateOfBirth ? this.formatDate(p.dateOfBirth) : null,
            doctorId: Number(realDoctorIdToSend), // <--- Gửi UserID của bác sĩ ở đây nữa
            appointmentTime: dateTimeStr
        };
        this.appointmentService.bookPublicAppointment(requestBody).subscribe({
            next: () => this.handleSuccess('home'),
            error: (err) => this.handleError(err)
        });
    }
  }

  handleSuccess(redirectType: 'admin' | 'home') {
      this.messageService.add({severity: 'success', summary: 'Thành công', detail: 'Đặt lịch thành công!'});
      this.showInfoDialog = false;
      const url = redirectType === 'admin' ? '/appointment-management' : '/';
      setTimeout(() => this.router.navigate([url]), 1500);
  }

  handleError(err: any) {
      this.messageService.add({severity: 'error', summary: 'Lỗi', detail: err.error?.message || 'Đặt lịch thất bại.'});
  }

  // --- UTILS ---
  loadServices() { this.masterDataService.getAllServices(1, 100).subscribe(res => this.services = res.result?.data || []); }

  loadDoctors(serviceId?: number) {
    this.staffService.getAllDoctors(1, 100).subscribe(res => {
      let allDoctors = res.result?.data || [];
      if (serviceId) {
        this.masterDataService.getAllSpecialties(1, 100).subscribe(specRes => {
          const specialties = specRes.result?.data || [];
          const targetSpec = specialties.find((s: any) => s.defaultServiceId == serviceId);
          this.doctors = targetSpec ? allDoctors.filter(d => d.specialtyId == targetSpec.specialtyId) : [];
        });
      } else {
        this.doctors = allDoctors;
      }
    });
  }

  closeTimeDialog() {
    this.showTimeDialog = false;
    // QUAN TRỌNG: Xóa tham số mode, date, time, doctorId khỏi URL
    // Khi null, router sẽ xóa param đó đi
    this.updateParams({ mode: null, date: null, time: null, doctorId: null });
  }

  // LOGIC AUTO-FILL QUAN TRỌNG
  checkUserLogin() {
    // 1. Kiểm tra cả 2 key phổ biến (đề phòng bạn lưu tên khác)
    const token = localStorage.getItem('access_token') || localStorage.getItem('token');

    if (token) {
        this.userService.getMyInfo().subscribe({
            next: (res) => {
                if (res.result) {
                    this.currentUser = res.result;
                    console.log('User found:', this.currentUser); // Debug: Xem log có hiện user không

                    // 2. Điền dữ liệu vào form
                    this.infoForm.patchValue({
                        fullName: this.currentUser.fullName || '', // Thêm || '' để tránh null
                        phoneNumber: this.currentUser.phoneNumber || '',
                        email: this.currentUser.email || '',
                        address: this.currentUser.address || '',
                        gender: this.currentUser.gender || 'MALE',
                        dateOfBirth: this.currentUser.dateOfBirth ? new Date(this.currentUser.dateOfBirth) : null
                    });

                    // 3. (Quan trọng) Disable các trường không nên sửa nếu là User thật
                    // Tùy ý bạn: Nếu muốn user không được sửa tên thì bỏ comment dòng dưới
                    // this.infoForm.get('fullName')?.disable();
                }
            },
            error: (err) => {
                console.error('Không lấy được thông tin user:', err);
                // Nếu token hết hạn hoặc lỗi, có thể xóa token đi
                // localStorage.removeItem('access_token');
            }
        });
    } else {
        console.log('Không tìm thấy token, coi như khách vãng lai.');
    }
  }

  formatDate(date: Date): string {
    if (!date) return '';
    const d = new Date(date);
    let month = '' + (d.getMonth() + 1);
    let day = '' + d.getDate();
    const year = d.getFullYear();
    if (month.length < 2) month = '0' + month;
    if (day.length < 2) day = '0' + day;
    return [year, month, day].join('-');
  }
}
