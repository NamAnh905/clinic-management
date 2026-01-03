import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';

//admin-layout
import { AdminLayoutComponent } from './layout/admin/admin-layout/admin-layout.component';
import { UserManagementComponent } from './features/admin/user-management/user-management.component';
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
import { PaymentReturnComponent } from './features/payment/payment-return/payment-return.component';

//public-layout
import { PublicLayoutComponent } from './layout/public/public-layout/public-layout.component';
import { HomeComponent } from './features/public/home/home.component';

//others
import { AuthGuard } from './core/guards/auth.guard';
import { RoleGuard } from './core/guards/role.guard';
import { AdminRedirectGuard } from './core/guards/admin-redirect.guard';
import { StatisticComponent } from './features/admin/statistic/statistic.component';

export const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  {
    path: 'payment-return',
    component: PaymentReturnComponent,
    title: 'Kết quả thanh toán'
  },
  //Public
  {
    path: '',
    component: PublicLayoutComponent,
    children: [
      {
        path: '',
        component: HomeComponent
      },
      {
        path: 'booking',
        loadComponent: () => import('./features/public/booking/booking.component').then(m => m.BookingComponent),
        title: 'Đặt lịch khám - 28Care'
      },
      {
        path: 'specialties',
        loadComponent: () => import('./features/public/specialty-list/specialty-list.component').then(m => m.SpecialtyListComponent)
      },
      {
        path: 'doctors',
        loadComponent: () => import('./features/public/doctor-list/doctor-list.component').then(m => m.DoctorListComponent)
      },
      {
        path: 'services',
        loadComponent: () => import('./features/public/service-list/service-list.component').then(m => m.ServiceListComponent)
      },
      // {
      //   path: 'news',
      //   // Nếu ông chưa tạo component News thì comment dòng dưới lại hoặc tạo nhanh: ng g c features/public/news
      //   loadComponent: () => import('./features/public/news/news.component').then(m => m.NewsComponent)
      // },
    ]
  },

  //Admin-management
  {
    path: 'admin',
    component: AdminLayoutComponent,
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
        component: StatisticComponent,
        canActivate: [RoleGuard]
      },
      {
        path: 'users',
        data: { breadcrumb: 'Quản lý người dùng', roles: ['ADMIN'] },
        component: UserManagementComponent,
        canActivate: [RoleGuard]
      },
      {
        path: 'staff',
        data: { breadcrumb: 'Quản lý nhân viên', roles: ['ADMIN'] },
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
        data: { breadcrumb: 'Hồ sơ bệnh án', roles: ['ADMIN', 'DOCTOR'] },
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
        data: { breadcrumb: 'Lịch làm việc', roles: ['ADMIN', 'DOCTOR', 'RECEPTIONIST'] },
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
  { path: '**', redirectTo: '' }
];
