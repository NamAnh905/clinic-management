import { ServiceType } from "./core.model";

// ================== SERVICE (DỊCH VỤ) ==================
export interface ServiceEntityResponse {
  serviceId: number;
  name: string;
  price: number;
  type: ServiceType;
}

export interface SECreationRequest {
  name: string;
  price: number;
  type: ServiceType;
}

export interface SEUpdationRequest {
  serviceId?: number;
  name?: string;
  price?: number;
}

// ================== DRUG (THUỐC) ==================
export interface DrugResponse {
  drugId: number;
  name: string;
  unit: string;
  instructions: string;
  stockQuantity: number;
  price: number;
}

export interface DrugCreationRequest {
  name: string;
  unit: string;
  instructions?: string;
  stockQuantity: number;
  price: number;
}

export interface DrugUpdateRequest {
  drugId?: number;
  name?: string;
  unit?: string;
  instructions?: string;
  stockQuantity?: number;
  price?: number;
}

// ================== SPECIALTY (CHUYÊN KHOA) ==================
export interface SpecialtyResponse {
  specialtyId: number;
  name: string;
  description: string;
}

export interface SpecialtyCreationRequest {
  name: string;
  description?: string;
  doctors?: number[]; // List ID bác sĩ
}

export interface SpecialtyUpdateRequest {
  name?: string;
  description?: string;
  doctors?: number[];
}
