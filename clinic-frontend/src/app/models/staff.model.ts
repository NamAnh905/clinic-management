import { Gender } from './core.model';

export interface DoctorResponse {
  doctorId: number;
  fullName?: string;
  userId: number;
  specialtyId: number;
  phoneNumber?: string;
  employeeCode: string;
  licenseNumber: string;
  specialtyName?: string;
}

export interface DoctorCreationRequest {
  userId: number;
  specialtyId: number;
  employeeCode?: string;
  licenseNumber?: string;
}

export interface DoctorUpdateRequest {
  specialtyId?: number;
  employeeCode?: string;
  licenseNumber?: string;
}

export interface ReceptionistResponse {
  receptionistId: number;
  employeeCode: string;
  fullName: string;
  email: string;
  phoneNumber: string;
  gender: Gender;
  dateOfBirth: string;
  hireDate: string;
}

export interface ReceptionistCreationRequest {
  userId: number;
  employeeCode?: string;
  hireDate: string;
}

export interface ReceptionistUpdateRequest {
  employeeCode?: string;
  hireDate?: string; // Dạng 'YYYY-MM-DD'
}
