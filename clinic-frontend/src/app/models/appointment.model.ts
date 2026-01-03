import { AppointmentStatus } from "./core.model";

// Hiển thị chi tiết
export interface AppointmentResponse {
  appointmentId: number;
  patientId: number;
  patientName: string;
  doctorId: number;
  doctorName: string;
  appointmentTime: string; // LocalDateTime string
  endTime: string;
  reason: string;
  status: AppointmentStatus;
}


// Form đặt lịch
export interface AppointmentCreationRequest {
  patientId: number; // Admin đặt thì cần, User đặt thì lấy từ Token
  doctorId: number;
  appointmentTime: string; // '2025-12-20T09:00:00'
  reason: string;
}

export interface PublicAppointmentRequest {
  // Thông tin cá nhân
  fullName: string;
  phoneNumber: string;
  email?: string; // Optional
  dateOfBirth?: string; // 'yyyy-MM-dd'
  gender?: string; // 'MALE', 'FEMALE', 'OTHER'
  address?: string;

  // Thông tin khám
  doctorId: number;
  appointmentTime: string; // 'yyyy-MM-ddTHH:mm:ss' (ISO string)
  reason?: string;
}

// 2. DTO gửi đi khi khách muốn hủy lịch
export interface CancelAppointmentRequest {
  appointmentId: number;
  phoneNumber: string;
  reason?: string;
}

// Form cập nhật lịch
export interface AppointmentUpdationRequest {
  appointmentTime?: string;
  reason?: string;
  status?: AppointmentStatus;
}
