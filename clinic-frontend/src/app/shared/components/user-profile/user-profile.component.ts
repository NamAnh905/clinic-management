import { Component, inject, OnInit } from '@angular/core';
import { CommonModule, formatDate } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

// PrimeNG
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { CalendarModule } from 'primeng/calendar';
import { DropdownModule } from 'primeng/dropdown';
import { MessageService } from 'primeng/api';

// Services
import { UserService } from '../../../api/user.service';
import { UploadService } from '../../../api/upload.service';

@Component({
  selector: 'app-user-profile',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, ButtonModule,
    InputTextModule, CalendarModule, DropdownModule
  ],
  providers: [MessageService],
  templateUrl: './user-profile.component.html',
  styleUrls: ['./user-profile.component.scss']
})
export class UserProfileComponent implements OnInit {
  profileForm: FormGroup;
  isUploading: boolean = false;
  uploadedImageUrl: string = '';
  currentUser: any = null;
  genderOptions = [{ label: 'Nam', value: 'MALE' }, { label: 'Nữ', value: 'FEMALE' }];

  private fb = inject(FormBuilder);
  private userService = inject(UserService);
  private uploadService = inject(UploadService);
  private messageService = inject(MessageService);

  constructor() {
    this.profileForm = this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(5)]],
      email: [{ value: '', disabled: true }],
      phoneNumber: ['', [Validators.pattern(/(84|0[3|5|7|8|9])+([0-9]{8})\b/)]],
      dateOfBirth: [null],
      gender: [null],
      address: ['']
    });
  }

  ngOnInit() {
    this.loadMyProfile();
  }

  loadMyProfile() {
    this.userService.getMyInfo().subscribe({
      next: (res) => {
        if (res.result) {
          this.currentUser = res.result;
          this.patchForm(this.currentUser);
        }
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Không tải được thông tin cá nhân' });
      }
    });
  }

  patchForm(user: any) {
    this.profileForm.patchValue({
        fullName: user.fullName,
        email: user.email,
        phoneNumber: user.phoneNumber,
        address: user.address,
        gender: user.gender,
        dateOfBirth: user.dateOfBirth ? new Date(user.dateOfBirth) : null
    });
    this.uploadedImageUrl = user.image || '';
  }

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      this.isUploading = true;
      this.uploadService.uploadImage(file).subscribe({
        next: (res) => {
          if (res.code === 1000) {
            this.uploadedImageUrl = res.result;
            this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Đã tải ảnh lên!' });
          }
          this.isUploading = false;
        },
        error: () => {
          this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Upload ảnh thất bại' });
          this.isUploading = false;
        }
      });
    }
  }

  saveProfile() {
    if (this.profileForm.invalid) return;
    const formValue = this.profileForm.getRawValue();
    const formattedDob = formValue.dateOfBirth ? formatDate(formValue.dateOfBirth, 'yyyy-MM-dd', 'en-US') : null;

    const updateData = {
      fullName: formValue.fullName,
      phoneNumber: formValue.phoneNumber,
      gender: formValue.gender,
      dateOfBirth: formattedDob,
      address: formValue.address,
      image: this.uploadedImageUrl
    };

    this.userService.updateMyInfo(updateData as any).subscribe({
      next: (res) => {
        this.messageService.add({ severity: 'success', summary: 'Thành công', detail: 'Cập nhật hồ sơ thành công!' });
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Cập nhật thất bại' });
      }
    });
  }
}
