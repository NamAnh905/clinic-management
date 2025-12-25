// ================== MEDICAL RECORD (BỆNH ÁN) ==================
export interface MedicalRecordResponse {
  recordId: number;
  appointmentId: number;
  patientName: string;
  height: number;
  weight: number;
  bloodPressure: string;
  temperature: number;
  heartRate: number;
  diagnosis: string;
  symptoms: string;
  treatmentPlan: string;
  visitDate: string;
  doctorId: number;
  doctorName: string;
}

export interface MedicalRecordCreationRequest {
  appointmentId: number;
  height?: number;
  weight?: number;
  bloodPressure?: string;
  temperature?: number;
  heartRate?: number;
  diagnosis: string;
  symptoms: string;
  treatmentPlan?: string;
}

export interface MedicalRecordUpdationRequest {
  height?: number;
  weight?: number;
  bloodPressure?: string;
  temperature?: number;
  heartRate?: number;
  diagnosis?: string;
  symptoms?: string;
  treatmentPlan?: string;
}

// ================== PRESCRIPTION (ĐƠN THUỐC) ==================

// Chi tiết 1 dòng thuốc trong đơn
export interface PresDetailResponse {
  detailId: number;
  drugName: string;
  unit: string;
  quantity: number;
  dosage: string;
}

export interface PrescriptionResponse {
  prescriptionId: number;
  recordId: number;
  doctorName: string;
  note: string;
  prescriptionDetails: PresDetailResponse[];
}

export interface PrescriptionCreationRequest {
  recordId: number;
  note?: string;
}

export interface PrescriptionUpdateRequest {
  note?: string;
}

// Tạo/Sửa chi tiết đơn thuốc
export interface PresDetailCreationRequest {
  prescriptionId: number;
  drugId: number;
  quantity: number;
  dosage: string;
}

export interface PresDetailUpdateRequest {
  quantity?: number;
  dosage?: string;
}
