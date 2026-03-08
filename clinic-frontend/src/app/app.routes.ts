import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { ForgotPasswordComponent } from './features/auth/forgot-password/forgot-password.component';
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
    path: 'forgot-password',
    component: ForgotPasswordComponent,
    title: 'Quên mật khẩu'
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
        data: { breadcrumb: 'Thống kê doanh thu', roles: ['ADMIN'] },
        loadComponent: () => import('./features/admin/statistic/statistic.component').then(m => m.StatisticComponent),
        title: "Thống kê doanh thu - 28Care",
        canActivate: [RoleGuard]
      },
      {
        path: 'users',
        data: { breadcrumb: 'Tài khoản người dùng', roles: ['ADMIN'] },
        loadComponent: () => import('./features/admin/user-management/user-management.component').then(m => m.UserManagementComponent),
        title: "Tài khoản người dùng - 28Care",
        canActivate: [RoleGuard]
      },
      {
        path: 'staff',
        data: { breadcrumb: 'Nhân sự', roles: ['ADMIN'] },
        canActivate: [RoleGuard],
        title: "Nhân sự - 28Care",
        children: [
          {
            path: 'doctors',
            data: { breadcrumb: 'Bác sĩ' },
            title: "Bác sĩ - 28Care",
            loadComponent: () => import('./features/admin/staff-management/doctor-management/doctor-management.component').then(m => m.DoctorManagementComponent)
          },
          {
            path: 'receptionists',
            data: { breadcrumb: 'Lễ tân' },
            title: "Lễ tân - 28Care",
            loadComponent: () => import('./features/admin/staff-management/receptionist-management/receptionist-management.component').then(m => m.ReceptionistManagementComponent)
          }
        ]
      },
      {
        path: 'patients',
        data: { breadcrumb: 'Hồ sơ bệnh nhân', roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST'] },
        loadComponent: () => import('./features/admin/patient-management/patient-management.component').then(m => m.PatientManagementComponent),
        title: "Hồ sơ bệnh nhân - 28Care",
        canActivate: [RoleGuard]
      },
      {
        path: 'records',
        data: { breadcrumb: 'Bệnh án điện tử', roles: ['ADMIN', 'DOCTOR'] },
        loadComponent: () => import('./features/admin/medical-management/medical-record.component').then(m => m.MedicalRecordComponent),
        title: "Bệnh án điện tử - 28Care",
        canActivate: [RoleGuard]
      },
      {
        path: 'specialties',
        data: { breadcrumb: 'Chuyên khoa', roles: ['ADMIN', 'RECEPTIONIST', 'DOCTOR'] },
        loadComponent: () => import('./features/admin/master-data-management/specialty-management/specialty-management.component').then(m => m.SpecialtyManagementComponent),
        title: "Chuyên khoa - 28Care",
        canActivate: [RoleGuard]
      },
      {
        path: 'schedule',
        data: { breadcrumb: 'Lịch làm việc', roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST'] },
        loadComponent: () => import('./features/admin/schedule-management/schedule-management.component').then(m => m.ScheduleManagementComponent),
        title: "Lịch làm việc - 28Care",
        canActivate: [RoleGuard]
      },
      {
        path: 'appointments',
        data: { breadcrumb: 'Lịch hẹn khám', roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST'] },
        loadComponent: () => import('./features/admin/appointment-management/appointment-management.component').then(m => m.AppointmentManagementComponent),
        title: "Lịch hẹn khám - 28Care",
        canActivate: [RoleGuard]
      },
      {
        path: 'services',
        data: { breadcrumb: 'Danh mục dịch vụ', roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST'] },
        loadComponent: () => import('./features/admin/master-data-management/service-management/service-management.component').then(m => m.ServiceManagementComponent),
        title: "Danh mục dịch vụ - 28Care",
        canActivate: [RoleGuard]
      },
      {
        path: 'drugs',
        data: { breadcrumb: 'Kho dược', roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST'] },
        loadComponent: () => import('./features/admin/master-data-management/drug-management/drug-management.component').then(m => m.DrugManagementComponent),
        title: "Kho dược - 28Care",
        canActivate: [RoleGuard]
      },
      {
        path: 'prescriptions',
        data: { breadcrumb: 'Kê đơn thuốc', roles: ['ADMIN', 'DOCTOR'] },
        loadComponent: () => import('./features/admin/prescription-management/prescription-management.component').then(m => m.PrescriptionComponent),
        title: "Kê đơn thuốc - 28Care",
        canActivate: [RoleGuard]
      },
      {
        path: 'invoices',
        data: { breadcrumb: 'Quản lý hóa đơn', roles: ['ADMIN', 'RECEPTIONIST'] },
        loadComponent: () => import('./features/admin/invoice-management/invoice-management.component').then(m => m.InvoiceManagementComponent),
        title: "Quản lý hóa đơn - 28Care",
        canActivate: [RoleGuard]
      }
    ]
  },
  { path: '**', redirectTo: '' }
];
