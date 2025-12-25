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

// Form cập nhật lịch
export interface AppointmentUpdationRequest {
  appointmentTime?: string;
  reason?: string;
  status?: AppointmentStatus;
}
