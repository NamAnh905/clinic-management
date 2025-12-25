import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { UserManagementComponent } from './features/admin/user-management/user-management.component';
import { AdminLayoutComponent } from './layout/admin/admin-layout/admin-layout.component';
import { ReceptionistManagementComponent } from './features/admin/staff-management/receptionist-management/receptionist-management.component';
import { DoctorManagementComponent } from './features/admin/staff-management/doctor-management/doctor-management.component';
import { SpecialtyManagementComponent } from './features/admin/master-data-management/specialty-management/specialty-management.component';
import { ScheduleManagementComponent } from './features/admin/schedule-management/schedule-management.component';
import { ServiceManagementComponent } from './features/admin/master-data-management/service-management/service-management.component';
import { DrugManagementComponent } from './features/admin/master-data-management/drug-management/drug-management.component';
import { AppointmentManagementComponent } from './features/admin/appointment-management/appointment-management.component';
import { PatientManagementComponent } from './features/admin/patient-management/patient-management.component';
import { MedicalRecordComponent } from './features/admin/medical-management/medical-record.component';
import { PrescriptionComponent } from './features/admin/prescription-management/prescription-management.component';
import { InvoiceManagementComponent } from './features/admin/invoice-management/invoice-management.component';

import { AuthGuard } from './core/guards/auth.guard';
import { RoleGuard } from './core/guards/role.guard';
import { AdminRedirectGuard } from './core/guards/admin-redirect.guard'; // Import Guard mới

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },

  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [AuthGuard], // Kiểm tra login chung cho cả cụm admin
    children: [
      // 1. Trang điều hướng mặc định (Thay thế cho redirectTo: 'users')
      {
        path: '',
        pathMatch: 'full',
        canActivate: [AdminRedirectGuard], // Guard này sẽ tự lái user đi đúng chỗ
        children: [] // Rỗng vì Guard sẽ redirect đi nơi khác ngay
      },

      // 2. Các trang con (Đã thêm phân quyền Role cụ thể)
      {
        path: 'users',
        data: { breadcrumb: 'Quản lý người dùng', roles: ['ADMIN'] }, // Chỉ Admin
        component: UserManagementComponent,
        canActivate: [RoleGuard]
      },
      {
        path: 'staff',
        data: { breadcrumb: 'Quản lý nhân viên', roles: ['ADMIN'] }, // Chỉ Admin
        canActivate: [RoleGuard],
        children: [
          { path: 'doctors', data: { breadcrumb: 'Bác sĩ' }, component: DoctorManagementComponent },
          { path: 'receptionists', data: { breadcrumb: 'Lễ tân' }, component: ReceptionistManagementComponent }
        ]
      },
      {
        path: 'patients',
        data: { breadcrumb: 'Quản lý bệnh nhân', roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST'] },
        component: PatientManagementComponent,
        canActivate: [RoleGuard]
      },
      {
        path: 'records',
        data: { breadcrumb: 'Hồ sơ bệnh án', roles: ['ADMIN', 'DOCTOR'] }, // Recep không xem
        component: MedicalRecordComponent,
        canActivate: [RoleGuard]
      },
      {
        path: 'specialties',
        data: { breadcrumb: 'Quản lý chuyên khoa', roles: ['ADMIN', 'RECEPTIONIST', 'DOCTOR'] },
        component: SpecialtyManagementComponent,
        canActivate: [RoleGuard]
      },
      {
        path: 'schedule',
        data: { breadcrumb: 'Lịch làm việc', roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST'] }, // Tạm để Admin theo Sidebar cũ
        component: ScheduleManagementComponent,
        canActivate: [RoleGuard]
      },
      {
        path: 'appointments',
        data: { breadcrumb: 'Lịch hẹn', roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST'] },
        component: AppointmentManagementComponent,
        canActivate: [RoleGuard]
      },
      {
        path: 'services',
        data: { breadcrumb: 'Quản lý dịch vụ', roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST'] },
        component: ServiceManagementComponent,
        canActivate: [RoleGuard]
      },
      {
        path: 'drugs',
        data: { breadcrumb: 'Quản lý thuốc', roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST'] },
        component: DrugManagementComponent,
        canActivate: [RoleGuard]
      },
      {
        path: 'prescriptions',
        data: { breadcrumb: 'Quản lý đơn thuốc', roles: ['ADMIN', 'DOCTOR'] },
        component: PrescriptionComponent,
        canActivate: [RoleGuard]
      },
      {
        path: 'invoices',
        data: { breadcrumb: 'Quản lý hóa đơn', roles: ['ADMIN', 'RECEPTIONIST'] },
        component: InvoiceManagementComponent,
        canActivate: [RoleGuard]
      }
    ]
  },

  { path: '**', redirectTo: 'login' }
];
