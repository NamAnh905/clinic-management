import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';

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

  constructor() {
    // Khởi tạo form với validator cơ bản
    this.infoForm = this.fb.group({
      fullName: ['', Validators.required],
      phoneNumber: ['', [Validators.required, Validators.pattern(/(84|0[3|5|7|8|9])+([0-9]{8})\b/)]],
      email: ['', [Validators.email]], // Thêm validate email
      dateOfBirth: [null], // Mặc định null, sẽ add validator sau nếu là khách
      gender: ['MALE'],
      address: [''],
      reason: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.loadServices();
    this.checkUserLogin(); // Check xong mới biết là User hay Guest
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

      this.masterDataService.getAllSpecialties(1, 100).subscribe(specRes => {
          const specialties = specRes.result?.data || [];
          const targetSpec = specialties.find((s: any) => s.defaultServiceId == this.selectedService.serviceId);

          if (targetSpec) {
              this.staffService.getAllDoctors(1, 100).subscribe(res => {
                  const docs = res.result?.data || [];
                  const availableDocs = docs.filter((d: any) => d.specialtyId == targetSpec.specialtyId);

                  if (availableDocs.length > 0) {
                      // --- SỬA Ở ĐÂY: Lưu lại bác sĩ được chọn ---
                      const chosenDoc = availableDocs[0];

                      this.doctors = docs; // Lưu danh sách để finalSubmit có thể tra cứu
                      this.selectedDoctor = chosenDoc; // Lưu bác sĩ để dùng cho confirmBooking

                      // Gọi API lấy slot theo Doctor ID (PK)
                      this.getSlots(chosenDoc.doctorId, dateStr);
                  } else {
                      this.availableSlots = [];
                      this.selectedDoctor = null; // Reset nếu không tìm thấy
                  }
              });
          } else {
              this.availableSlots = [];
          }
      });
  }

  getSlots(docId: number, dateStr: string) {
    this.appointmentService.getAvailableSlots(docId, dateStr).subscribe(res => {
      let slots = res.result || [];

      // 1. Kiểm tra xem ngày được chọn có phải là "Hôm nay" không
      const now = new Date();
      // Lưu ý: this.selectedDate là đối tượng Date đang được binding với p-calendar
      const checkDate = new Date(this.selectedDate);

      const isToday = checkDate.getDate() === now.getDate() &&
                      checkDate.getMonth() === now.getMonth() &&
                      checkDate.getFullYear() === now.getFullYear();

      // 2. Nếu là hôm nay, lọc bỏ các slot đã qua hoặc hiện tại
      if (isToday) {
        const currentHour = now.getHours();
        const currentMinute = now.getMinutes();

        slots = slots.filter(slot => {
          // slot string có dạng "09:30"
          const parts = slot.split(':');
          const slotHour = parseInt(parts[0], 10);
          const slotMinute = parseInt(parts[1], 10);

          // Logic:
          // - Nếu giờ slot > giờ hiện tại -> Lấy
          // - Nếu giờ slot == giờ hiện tại -> Phút slot phải lớn hơn phút hiện tại
          if (slotHour > currentHour) return true;
          if (slotHour === currentHour && slotMinute > currentMinute) return true;

          return false;
        });
      }

      this.availableSlots = slots;
    });
  }

  selectTime(time: string) {
    this.selectedTime = time;
    this.updateParams({ date: this.formatDate(this.selectedDate), time: time });
  }

  // --- LOGIC XÁC NHẬN ĐẶT LỊCH ---
  confirmBooking() {
    // 1. Validate Form
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

    // 2. Kiểm tra xem đã có bác sĩ được chọn chưa
    // (Dù là mode TIME hay DOCTOR, thì logic ở bước chọn ngày/giờ đã phải gán selectedDoctor rồi)
    if (!this.selectedDoctor || !this.selectedDoctor.doctorId) {
        this.messageService.add({
            severity: 'error',
            summary: 'Lỗi',
            detail: 'Chưa xác định được bác sĩ cho lịch khám này. Vui lòng chọn lại ngày giờ.'
        });
        return;
    }

    // 3. Gọi finalSubmit với ID của bác sĩ đã chọn
    this.finalSubmit(this.selectedDoctor.doctorId);
  }

  // LOGIC AUTO-FILL QUAN TRỌNG
  checkUserLogin() {
      const token = localStorage.getItem('token');
      if (token) {
          this.userService.getMyInfo().subscribe({
              next: (res) => {
                  if (res.result) {
                      this.currentUser = res.result;

                      // 1. Tự động điền (Auto-fill)
                      this.infoForm.patchValue({
                          fullName: this.currentUser.fullName,
                          phoneNumber: this.currentUser.phoneNumber,
                          email: this.currentUser.email,
                          address: this.currentUser.address,
                          gender: this.currentUser.gender || 'MALE',
                          // Map date string sang Date object cho Calendar hiển thị
                          dateOfBirth: this.currentUser.dateOfBirth ? new Date(this.currentUser.dateOfBirth) : null
                      });

                      // 2. TẮT bắt buộc nhập Ngày sinh (Vì User đã có trong DB rồi)
                      // Giúp User không bị lỗi form nếu họ lỡ xóa ngày sinh trên giao diện
                      this.infoForm.get('dateOfBirth')?.clearValidators();
                      this.infoForm.get('dateOfBirth')?.updateValueAndValidity();
                  }
              },
              error: () => {
                  // Token lỗi -> Coi như khách
                  this.currentUser = null;
                  this.setupGuestValidators();
              }
          });
      } else {
          // Không có token -> Là khách
          this.currentUser = null;
          this.setupGuestValidators();
      }
  }

  finalSubmit(doctorId: number) {
    if (this.infoForm.invalid) return; // Đã check ở trên rồi nhưng giữ lại cũng được

    const p = this.infoForm.value;
    const dateTimeStr = `${this.formatDate(this.selectedDate)}T${this.selectedTime}:00`;

    // Tìm object bác sĩ để lấy UserID (Quan trọng cho luồng Auth)
    const selectedDocObj = this.doctors.find(d => d.doctorId === doctorId);

    // --- TRƯỜNG HỢP 1: CÓ USER LOGIN ---
    if (this.currentUser && this.currentUser.userId) {
        const doctorUserId = selectedDocObj ? (selectedDocObj.userId || selectedDocObj.user_id) : null;

        // --- SỬA LỖI: Thêm thông báo nếu không tìm thấy ID ---
        if (!doctorUserId) {
            console.error("Lỗi: Không tìm thấy UserID của bác sĩ trong danh sách", this.doctors);
            this.messageService.add({
                severity: 'error',
                summary: 'Lỗi hệ thống',
                detail: 'Không lấy được thông tin bác sĩ. Vui lòng tải lại trang.'
            });
            return;
        }

        const requestBody = {
            patientId: Number(this.currentUser.userId),
            doctorId: Number(doctorUserId), // Gửi UserID
            appointmentTime: dateTimeStr,
            reason: p.reason
        };

        this.appointmentService.bookAppointment(requestBody).subscribe({
            next: () => this.handleSuccess('admin'),
            error: (err) => this.handleError(err)
        });
    }
    // --- TRƯỜNG HỢP 2: KHÁCH VÃNG LAI (PUBLIC) ---
    else {
        // Fix lỗi null dateOfBirth như đã bàn trước đó
        const dobFormatted = p.dateOfBirth ? this.formatDate(p.dateOfBirth) : '';

        const requestBody = {
            fullName: p.fullName,
            phoneNumber: p.phoneNumber,
            email: p.email,
            dateOfBirth: dobFormatted,
            gender: p.gender,
            address: p.address,
            doctorId: Number(doctorId), // Gửi DoctorID (PK)
            appointmentTime: dateTimeStr,
            reason: p.reason
        };

        this.appointmentService.bookPublicAppointment(requestBody).subscribe({
            next: () => this.handleSuccess('home'),
            error: (err) => this.handleError(err)
        });
    }
  }

  handleSuccess(redirectType: 'admin' | 'home') {
      let detailMessage = '';

      // TRƯỜNG HỢP 1: Khách hàng ĐÃ đăng nhập (User cũ)
      if (redirectType === 'admin') {
          detailMessage = 'Đặt lịch thành công! Chi tiết cuộc hẹn đã được gửi tới email của bạn.';
      }
      // TRƯỜNG HỢP 2: Khách vãng lai (Guest)
      else {
          detailMessage = 'Đặt lịch thành công! Vui lòng kiểm tra email để nhận Lịch hẹn và Tài khoản đăng nhập.';
      }

      this.messageService.add({
          severity: 'success',
          summary: 'Thành công',
          detail: detailMessage // Sử dụng nội dung linh hoạt
      });

      this.showInfoDialog = false;
      const url = redirectType === 'admin' ? '/appointment-management' : '/';

      // Tăng thời gian delay một chút để người dùng kịp đọc thông báo dài hơn
      setTimeout(() => this.router.navigate([url]), 3000);
  }

  handleError(err: any) {
      this.messageService.add({severity: 'error', summary: 'Lỗi', detail: err.error?.message || 'Đặt lịch thất bại.'});
  }

  // --- UTILS ---
  loadServices() { this.masterDataService.getAllServices(1, 100).subscribe(res => this.services = res.result?.data || []); }

  setupGuestValidators() {
      this.infoForm.reset();
      this.infoForm.patchValue({ gender: 'MALE' });

      // 1. Ngày sinh: BẮT BUỘC (Backend có @NotNull)
      this.infoForm.get('dateOfBirth')?.setValidators([Validators.required]);

      // 2. Email: KHÔNG BẮT BUỘC (Backend tự handle nếu null)
      // Chỉ validate đúng định dạng email nếu người dùng có nhập
      this.infoForm.get('email')?.setValidators([Validators.email]);

      this.infoForm.get('dateOfBirth')?.updateValueAndValidity();
      this.infoForm.get('email')?.updateValueAndValidity();
  }

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
