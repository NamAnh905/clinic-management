import { Gender } from './core.model';

export interface AuthenticationRequest {
  email?: string;
  password?: string;
  token?: string; // Dùng cho trường hợp login bằng Google/Facebook sau này nếu có
}

export interface AuthenticationResponse {
  token: string;
  authenticated: boolean;
}

export interface RegisterRequest {
  fullName: string;
  email: string;
  password: string;
  phoneNumber?: string;
  address?: string;
  gender: Gender;
  dateOfBirth: string; // Dạng chuỗi 'YYYY-MM-DD'
}

export interface UserUpdateRequest {
  fullName?: string;
  password?: string;
  phoneNumber?: string;
  gender?: Gender;
  dateOfBirth?: string;
  address?: string;
  roles?: string[];
  isActive?: boolean;
}

export interface IntrospectRequest {
  token: string;
}

export interface IntrospectResponse {
  valid: boolean;
}
