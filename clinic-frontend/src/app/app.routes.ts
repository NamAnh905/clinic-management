import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { PaymentReturnComponent } from './features/payment/payment-return/payment-return.component';

// public-layout
import { PublicLayoutComponent } from './layout/public/public-layout/public-layout.component';
import { HomeComponent } from './features/public/home/home.component';
import { UserDashboardComponent } from './features/public/user-dashboard/user-dashboard.component';

// guards (Giữ nguyên import các Guard vì chúng cần thiết để check quyền trước khi tải component)
import { AuthGuard } from './core/guards/auth.guard';
import { RoleGuard } from './core/guards/role.guard';
import { AdminRedirectGuard } from './core/guards/admin-redirect.guard';

export const routes: Routes = [
  {
    path: 'login',
    component: LoginComponent,
    title: 'Đăng nhập'
  },
  {
    path: 'register',
    component: RegisterComponent,
    title: 'Đăng ký'
  },
  {
    path: 'payment-return',
    component: PaymentReturnComponent,
    title: 'Kết quả thanh toán'
  },

  // --- Public Routes ---
  {
    path: '',
    component: PublicLayoutComponent,
    children: [
      {
        path: '',
        component: HomeComponent,
        title: 'Trang chủ'
      },
      {
        path: 'about-us',
        loadComponent: () => import('./features/public/about-us/about-us.component').then(m => m.AboutUsComponent),
        title: 'Giới thiệu - 28Care'
      },
      {
        path: 'privacy-policy',
        loadComponent: () => import('./features/public/privacy-policy/privacy-policy.component').then(m => m.PrivacyPolicyComponent),
        title: 'Chính sách bảo mật - 28Care'
      },
      {
        path: 'booking',
        loadComponent: () => import('./features/public/booking/booking.component').then(m => m.BookingComponent),
        title: 'Đặt lịch khám - 28Care'
      },
      {
        path: 'specialties',
        loadComponent: () => import('./features/public/specialty-list/specialty-list.component').then(m => m.SpecialtyListComponent),
        title: 'Chuyên khoa'
      },
      {
        path: 'doctors',
        loadComponent: () => import('./features/public/doctor-list/doctor-list.component').then(m => m.DoctorListComponent),
        title: 'Bác sĩ'
      },
      {
        path: 'services',
        loadComponent: () => import('./features/public/service-list/service-list.component').then(m => m.ServiceListComponent),
        title: 'Dịch vụ'
      },
      {
        path: 'profile',
        component: UserDashboardComponent,
        canActivate: [AuthGuard],
        children: [
          { path: '', redirectTo: 'account', pathMatch: 'full' },
          {
            path: 'account',
            loadComponent: () => import('./shared/components/user-profile/user-profile.component').then(m => m.UserProfileComponent),
            title: 'Hồ sơ của tôi'
          },
          {
            path: 'appointments',
            loadComponent: () => import('./shared/components/user-appointment/user-appointment.component').then(m => m.UserAppointmentComponent),
            title: 'Lịch sử khám bệnh'
          },
          {
              path: 'prescriptions',
              loadComponent: () => import('./shared/components/user-prescription/user-prescription.component').then(m => m.UserPrescriptionComponent),
              title: 'Đơn thuốc cá nhân'
          },
          {
            path: 'invoices',
            loadComponent: () => import('./shared/components/user-invoice/user-invoice.component').then(m => m.UserInvoiceComponent),
            title: 'Lịch sử thanh toán'
          },
          {
             path: 'password',
             loadComponent: () => import('./shared/components/user-password/user-password.component').then(m => m.UserPasswordComponent),
             title: 'Đổi mật khẩu'
          }
        ]
      },
    ]
  },

  // --- Admin Management (Đã tối ưu Lazy Loading) ---
  {
    path: 'admin',
    loadComponent: () => import('./layout/admin/admin-layout/admin-layout.component').then(m => m.AdminLayoutComponent),
    canActivate: [AuthGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        canActivate: [AdminRedirectGuard],
        children: []
      },
      {
        path: 'revenue',
        data: { breadcrumb: 'Quản lý doanh thu', roles: ['ADMIN'] },
        loadComponent: () => import('./features/admin/statistic/statistic.component').then(m => m.StatisticComponent),
        canActivate: [RoleGuard]
      },
      {
        path: 'users',
        data: { breadcrumb: 'Quản lý người dùng', roles: ['ADMIN'] },
        loadComponent: () => import('./features/admin/user-management/user-management.component').then(m => m.UserManagementComponent),
        canActivate: [RoleGuard]
      },
      {
        path: 'staff',
        data: { breadcrumb: 'Quản lý nhân viên', roles: ['ADMIN'] },
        canActivate: [RoleGuard],
        children: [
          {
            path: 'doctors',
            data: { breadcrumb: 'Bác sĩ' },
            loadComponent: () => import('./features/admin/staff-management/doctor-management/doctor-management.component').then(m => m.DoctorManagementComponent)
          },
          {
            path: 'receptionists',
            data: { breadcrumb: 'Lễ tân' },
            loadComponent: () => import('./features/admin/staff-management/receptionist-management/receptionist-management.component').then(m => m.ReceptionistManagementComponent)
          }
        ]
      },
      {
        path: 'patients',
        data: { breadcrumb: 'Quản lý bệnh nhân', roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST'] },
        loadComponent: () => import('./features/admin/patient-management/patient-management.component').then(m => m.PatientManagementComponent),
        canActivate: [RoleGuard]
      },
      {
        path: 'records',
        data: { breadcrumb: 'Hồ sơ bệnh án', roles: ['ADMIN', 'DOCTOR'] },
        loadComponent: () => import('./features/admin/medical-management/medical-record.component').then(m => m.MedicalRecordComponent),
        canActivate: [RoleGuard]
      },
      {
        path: 'specialties',
        data: { breadcrumb: 'Quản lý chuyên khoa', roles: ['ADMIN', 'RECEPTIONIST', 'DOCTOR'] },
        loadComponent: () => import('./features/admin/master-data-management/specialty-management/specialty-management.component').then(m => m.SpecialtyManagementComponent),
        canActivate: [RoleGuard]
      },
      {
        path: 'schedule',
        data: { breadcrumb: 'Lịch làm việc', roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST'] },
        loadComponent: () => import('./features/admin/schedule-management/schedule-management.component').then(m => m.ScheduleManagementComponent),
        canActivate: [RoleGuard]
      },
      {
        path: 'appointments',
        data: { breadcrumb: 'Lịch hẹn', roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST'] },
        loadComponent: () => import('./features/admin/appointment-management/appointment-management.component').then(m => m.AppointmentManagementComponent),
        canActivate: [RoleGuard]
      },
      {
        path: 'services',
        data: { breadcrumb: 'Quản lý dịch vụ', roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST'] },
        loadComponent: () => import('./features/admin/master-data-management/service-management/service-management.component').then(m => m.ServiceManagementComponent),
        canActivate: [RoleGuard]
      },
      {
        path: 'drugs',
        data: { breadcrumb: 'Quản lý thuốc', roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST'] },
        loadComponent: () => import('./features/admin/master-data-management/drug-management/drug-management.component').then(m => m.DrugManagementComponent),
        canActivate: [RoleGuard]
      },
      {
        path: 'prescriptions',
        data: { breadcrumb: 'Quản lý đơn thuốc', roles: ['ADMIN', 'DOCTOR'] },
        loadComponent: () => import('./features/admin/prescription-management/prescription-management.component').then(m => m.PrescriptionComponent),
        canActivate: [RoleGuard]
      },
      {
        path: 'invoices',
        data: { breadcrumb: 'Quản lý hóa đơn', roles: ['ADMIN', 'RECEPTIONIST'] },
        loadComponent: () => import('./features/admin/invoice-management/invoice-management.component').then(m => m.InvoiceManagementComponent),
        canActivate: [RoleGuard]
      }
    ]
  },
  { path: '**', redirectTo: '' }
];
