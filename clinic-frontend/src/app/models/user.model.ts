import { Gender } from './core.model';

export interface PermissionResponse {
  name: string;
  description: string;
}

export interface RoleResponse {
  name: string;
  description: string;
  permissions?: PermissionResponse[]; // Có thể có hoặc không
}

export interface UserResponse {
  userId: number;
  fullName: string;
  email: string;
  phoneNumber: string;
  gender: Gender;
  dateOfBirth: string;
  address: string;
  roles: RoleResponse[];
  isActive: boolean;
}
