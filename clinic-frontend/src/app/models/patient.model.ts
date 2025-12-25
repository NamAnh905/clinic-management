import { Gender } from './core.model';

export interface PatientResponse {
  patientId: number;
  userId: number;
  patientName: string;
  email: string;
  phoneNumber: string;
  gender: Gender;
  dateOfBirth: string;
  address: string;
  medicalHistory: string;
}

export interface PatientCreationRequest {
  userId: number;
  medicalHistory?: string;
}

export interface PatientUpdationRequest {
  fullName?: string;
  phoneNumber?: string;
  gender?: Gender;
  dateOfBirth?: string;
  address?: string;
  medicalHistory?: string;
}
