import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { CommonModule, formatDate } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';

// PrimeNG
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { InputTextModule } from 'primeng/inputtext';
import { CalendarModule } from 'primeng/calendar';
import { DropdownModule } from 'primeng/dropdown';
import { MessageService } from 'primeng/api';

// Services
import { UserService } from '../../../core/services/user.service';
import { UploadService } from '../../../core/services/upload.service';

@Component({
  selector: 'app-profile-update-dialog',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, DialogModule, ButtonModule,
    InputTextModule, CalendarModule, DropdownModule
  ],
  templateUrl: './profile-update-dialog.component.html',
  styleUrls: ['./profile-update-dialog.component.scss']
})
export class ProfileUpdateDialogComponent {
  @Input() visible: boolean = false;
  @Input() currentUser: any;
  @Output() visibleChange = new EventEmitter<boolean>();
  @Output() onUpdateSuccess = new EventEmitter<any>();

  profileForm: FormGroup;
  isUploading: boolean = false;
  uploadedImageUrl: string = '';
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

  ngOnChanges() {
    if (this.visible && this.currentUser) {
      this.profileForm.patchValue({
        fullName: this.currentUser.fullName,
        email: this.currentUser.email,
        phoneNumber: this.currentUser.phoneNumber,
        address: this.currentUser.address,
        gender: this.currentUser.gender,
        dateOfBirth: this.currentUser.dateOfBirth ? new Date(this.currentUser.dateOfBirth) : null
      });
      this.uploadedImageUrl = this.currentUser.image || '';
    }
  }

  close() {
    this.visible = false;
    this.visibleChange.emit(false);
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
        this.onUpdateSuccess.emit(res.result);
        this.close();
      },
      error: () => {
        this.messageService.add({ severity: 'error', summary: 'Lỗi', detail: 'Cập nhật thất bại' });
      }
    });
  }
}
