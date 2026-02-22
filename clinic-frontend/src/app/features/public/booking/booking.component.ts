import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { forkJoin, of, Observable } from 'rxjs';
import { catchError } from 'rxjs/operators';

// PrimeNG Imports
import { ButtonModule } from 'primeng/button';
import { RadioButtonModule } from 'primeng/radiobutton';
import { CalendarModule } from 'primeng/calendar';
import { DialogModule } from 'primeng/dialog';
import { ToastModule } from 'primeng/toast';
import { InputTextModule } from 'primeng/inputtext';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { MessageService } from 'primeng/api';

// Services
import { MasterDataService } from '../../../api/master-data.service';
import { StaffService } from '../../../api/staff.service';
import { AppointmentService } from '../../../api/appointment.service';
import { UserService } from '../../../api/user.service';

@Component({
  selector: 'app-booking',
  standalone: true,
  imports: [
    CommonModule, FormsModule, ReactiveFormsModule,
    ButtonModule, CalendarModule, DialogModule, ToastModule,
    InputTextModule, InputTextareaModule, RadioButtonModule
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

  slotToDoctorMap: { [time: string]: number[] } = {};

  constructor() {
    this.infoForm = this.fb.group({
      fullName: ['', Validators.required],
      phoneNumber: ['', [Validators.required, Validators.pattern(/(84|0[3|5|7|8|9])+([0-9]{8})\b/)]],
      email: ['', [Validators.email]],
      dateOfBirth: [null],
      gender: ['MALE'],
      address: [''],
      reason: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.loadServices();
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

             // Fix 1: Tránh gọi API lấy slot nếu đã có sẵn date và time trên URL
             if (!params['date'] || !params['time']) {
                 this.onDateSelect();
             }
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

      // Xử lý mở Info Dialog khi có đủ thông tin
      if (params['date'] && params['time']) {
          this.selectedDate = new Date(params['date']);
          this.selectedTime = params['time'];
          this.showTimeDialog = false;

          // Đảm bảo selectedDoctor đã được load trước khi hiện Dialog Info
          if (params['doctorId'] && !this.selectedDoctor) {
              if (this.doctors.length === 0) {
                  this.staffService.getAllDoctors(1, 100).subscribe((res: any) => {
                      this.doctors = res.result?.data || [];
                      this.selectedDoctor = this.doctors.find((d: any) => d.doctorId == params['doctorId']);
                      this.showInfoDialog = true;
                      this.progress = 95;
                  });
              } else {
                  this.selectedDoctor = this.doctors.find(d => d.doctorId == params['doctorId']);
                  this.showInfoDialog = true;
                  this.progress = 95;
              }
          } else {
              this.showInfoDialog = true;
              this.progress = 95;
          }
      }
    }
  }

  filterValidSlots(slots: string[]): string[] {
    const now = new Date();
    const checkDate = new Date(this.selectedDate);

    const isToday = checkDate.getDate() === now.getDate() &&
                    checkDate.getMonth() === now.getMonth() &&
                    checkDate.getFullYear() === now.getFullYear();

    if (!isToday) return slots;

    const bufferTime = new Date(now.getTime() + 15 * 60000);
    const bufferHour = bufferTime.getHours();
    const bufferMinute = bufferTime.getMinutes();

    return slots.filter((slot: string) => {
      const parts = slot.split(':');
      const slotHour = parseInt(parts[0], 10);
      const slotMinute = parseInt(parts[1], 10);

      if (slotHour > bufferHour) return true;
      if (slotHour === bufferHour && slotMinute > bufferMinute) return true;

      return false;
    });
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
    this.doctors = [];
    this.selectedDoctor = null;

    this.selectedService = service;
    this.updateParams({ serviceId: service.serviceId, mode: null });
    this.showModeDialog = true;
  }

  confirmMode(mode: 'TIME' | 'DOCTOR') {
    this.bookingMode = mode;
    this.updateParams({ mode: mode });

    if (mode === 'TIME') {
        this.showTimeDialog = true;
        if (this.availableSlots.length === 0) this.onDateSelect();
    }
  }

  selectDoctor(doc: any) {
    this.selectedDoctor = doc;
    this.updateParams({ doctorId: doc.doctorId });
    this.showTimeDialog = true;
    this.onDateSelect();
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

      this.masterDataService.getAllSpecialties(1, 100).subscribe((specRes: any) => {
          const specialties = specRes.result?.data || [];
          const targetSpec = specialties.find((s: any) => s.defaultServiceId == this.selectedService.serviceId);

          if (targetSpec) {
              this.staffService.getAllDoctors(1, 100).subscribe((res: any) => {
                  const docs = res.result?.data || [];
                  const availableDocs = docs.filter((d: any) => d.specialtyId == targetSpec.specialtyId);

                  if (availableDocs.length > 0) {
                      this.doctors = availableDocs;
                      this.selectedDoctor = null; // Chưa chọn bác sĩ vội ở bước này

                      // Tạo mảng các request lấy lịch của TẤT CẢ bác sĩ hợp lệ
                      const slotRequests: Observable<any>[] = availableDocs.map((doc: any) =>
                          this.appointmentService.getAvailableSlots(doc.doctorId, dateStr).pipe(
                              catchError(() => of({ result: [] }))
                          )
                      );

                      // Chạy tất cả request cùng lúc
                        forkJoin(slotRequests).subscribe((responses: any[]) => {
                            const aggregatedSlots = new Set<string>(); // Dùng Set để tránh trùng lặp khung giờ
                            this.slotToDoctorMap = {}; // Reset map

                          responses.forEach((res, index) => {
                              const docId = availableDocs[index].doctorId;
                              let slots: string[] = res.result || [];

                              // Lọc bỏ các giờ đã qua nếu là ngày hôm nay
                              slots = this.filterValidSlots(slots);

                              slots.forEach(slot => {
                                  aggregatedSlots.add(slot); // Thêm giờ vào danh sách hiển thị

                                  // Map giờ này với ID của bác sĩ
                                  if (!this.slotToDoctorMap[slot]) {
                                      this.slotToDoctorMap[slot] = [];
                                  }
                                  this.slotToDoctorMap[slot].push(docId);
                              });
                          });

                          // Cập nhật UI: Sắp xếp các khung giờ từ sớm đến muộn
                          this.availableSlots = Array.from(aggregatedSlots).sort();
                      });

                  } else {
                      this.availableSlots = [];
                      this.selectedDoctor = null;
                  }
              });
          } else {
              this.availableSlots = [];
          }
      });
  }

  getSlots(docId: number, dateStr: string) {
    this.appointmentService.getAvailableSlots(docId, dateStr).subscribe((res: any) => {
      let slots = res.result || [];
      this.availableSlots = this.filterValidSlots(slots);
    });
  }

  selectTime(time: string) {
    this.selectedTime = time;

    // Nếu đang ở mode chọn theo THỜI GIAN, tiến hành Auto-fill ngẫu nhiên bác sĩ
    if (this.bookingMode === 'TIME' && this.slotToDoctorMap[time] && this.slotToDoctorMap[time].length > 0) {
        // Lấy danh sách ID các bác sĩ đang rảnh ở khung giờ này
        const availableDocIds = this.slotToDoctorMap[time];

        // Random 1 vị trí (index) trong mảng
        const randomIndex = Math.floor(Math.random() * availableDocIds.length);
        const assignedDocId = availableDocIds[randomIndex];

        // Gán bác sĩ đó vào selectedDoctor để hiển thị lên UI
        this.selectedDoctor = this.doctors.find(d => d.doctorId === assignedDocId);

        this.updateParams({
            date: this.formatDate(this.selectedDate),
            time: time,
            doctorId: assignedDocId
        });
    } else {
        // Flow chọn theo bác sĩ bình thường
        this.updateParams({
            date: this.formatDate(this.selectedDate),
            time: time
        });
    }
  }

  // --- LOGIC XÁC NHẬN ĐẶT LỊCH ---
  confirmBooking() {
    if (this.infoForm.invalid) {
        this.infoForm.markAllAsTouched();
        this.messageService.add({
            severity: 'warn',
            summary: 'Thông tin chưa đủ',
            detail: 'Vui lòng kiểm tra lại các trường báo đỏ.'
        });
        return;
    }

    this.progress = 100;

    if (!this.selectedDoctor || !this.selectedDoctor.doctorId) {
        this.messageService.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Chưa xác định được bác sĩ cho lịch khám này. Vui lòng chọn lại ngày giờ.'
        });
        return;
    }

    this.finalSubmit(this.selectedDoctor.doctorId);
  }

  checkUserLogin() {
      const token = localStorage.getItem('token');
      if (token) {
          this.userService.getMyInfo().subscribe({
              next: (res: any) => {
                  if (res.result) {
                      this.currentUser = res.result;

                      this.infoForm.patchValue({
                          fullName: this.currentUser.fullName,
                          phoneNumber: this.currentUser.phoneNumber,
                          email: this.currentUser.email,
                          address: this.currentUser.address,
                          gender: this.currentUser.gender || 'MALE',
                          dateOfBirth: this.currentUser.dateOfBirth ? new Date(this.currentUser.dateOfBirth) : null
                      });

                      this.infoForm.get('dateOfBirth')?.clearValidators();
                      this.infoForm.get('dateOfBirth')?.updateValueAndValidity();
                  }
              },
              error: () => {
                  this.currentUser = null;
                  this.setupGuestValidators();
              }
          });
      } else {
          this.currentUser = null;
          this.setupGuestValidators();
      }
  }

  finalSubmit(doctorId: number) {
    if (this.infoForm.invalid) return;

    const p = this.infoForm.value;
    const dateTimeStr = `${this.formatDate(this.selectedDate)}T${this.selectedTime}:00`;

    if (this.currentUser && this.currentUser.userId) {
        // Fix 4: Gửi trực tiếp doctorId thay vì doctorUserId
        const requestBody = {
            patientId: Number(this.currentUser.userId),
            doctorId: Number(doctorId),
            appointmentTime: dateTimeStr,
            reason: p.reason
        };

        this.appointmentService.bookAppointment(requestBody).subscribe({
            next: () => this.handleSuccess('admin'),
            error: (err: any) => this.handleError(err)
        });
    }
    else {
        const dobFormatted = p.dateOfBirth ? this.formatDate(p.dateOfBirth) : '';

        const requestBody = {
            fullName: p.fullName,
            phoneNumber: p.phoneNumber,
            email: p.email,
            dateOfBirth: dobFormatted,
            gender: p.gender,
            address: p.address,
            doctorId: Number(doctorId),
            appointmentTime: dateTimeStr,
            reason: p.reason
        };

        this.appointmentService.bookPublicAppointment(requestBody).subscribe({
            next: () => this.handleSuccess('home'),
            error: (err: any) => this.handleError(err)
        });
    }
  }

  handleSuccess(redirectType: 'admin' | 'home') {
      let detailMessage = '';

      if (redirectType === 'admin') {
          detailMessage = 'Đặt lịch thành công! Chi tiết cuộc hẹn đã được gửi tới email của bạn.';
      }
      else {
          detailMessage = 'Đặt lịch thành công! Vui lòng kiểm tra email để nhận Lịch hẹn và Tài khoản đăng nhập.';
      }

      this.messageService.add({
          severity: 'success',
          summary: 'Thành công',
          detail: detailMessage
      });

      this.showInfoDialog = false;
      const url = redirectType === 'admin' ? '/appointment-management' : '/';

      setTimeout(() => this.router.navigate([url]), 3000);
  }

  handleError(err: any) {
      this.messageService.add({severity: 'error', summary: 'Lỗi', detail: err.error?.message || 'Đặt lịch thất bại.'});
  }

  // --- UTILS ---
  loadServices() { this.masterDataService.getAllServices(1, 100).subscribe((res: any) => this.services = res.result?.data || []); }

  setupGuestValidators() {
      this.infoForm.reset();
      this.infoForm.patchValue({ gender: 'MALE' });

      this.infoForm.get('dateOfBirth')?.setValidators([Validators.required]);
      this.infoForm.get('email')?.setValidators([Validators.email]);

      this.infoForm.get('dateOfBirth')?.updateValueAndValidity();
      this.infoForm.get('email')?.updateValueAndValidity();
  }

  loadDoctors(serviceId?: number) {
    this.staffService.getAllDoctors(1, 100).subscribe((res: any) => {
      let allDoctors = res.result?.data || [];
      if (serviceId) {
        this.masterDataService.getAllSpecialties(1, 100).subscribe((specRes: any) => {
          const specialties = specRes.result?.data || [];
          const targetSpec = specialties.find((s: any) => s.defaultServiceId == serviceId);
          this.doctors = targetSpec ? allDoctors.filter((d: any) => d.specialtyId == targetSpec.specialtyId) : [];
        });
      } else {
        this.doctors = allDoctors;
      }
    });
  }

  closeTimeDialog() {
    this.showTimeDialog = false;
    this.updateParams({ mode: null, date: null, time: null, doctorId: null });
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
