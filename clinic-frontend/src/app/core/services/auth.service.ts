import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable, lastValueFrom, of } from 'rxjs';
import { tap, catchError, map, switchMap } from 'rxjs/operators';
import { environment } from '../../../environments/environment';
import { ApiResponse } from '../../models/core.model';
import { AuthenticationRequest, AuthenticationResponse, IntrospectResponse } from '../../models/auth.model';
import { UserResponse } from '../../models/user.model';
import { UserService } from './user.service';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private authUrl = `${environment.apiUrl}/auth`;

  // 1. Biến lưu User hiện tại
  private currentUserSubject = new BehaviorSubject<UserResponse | null>(null);
  public currentUser$ = this.currentUserSubject.asObservable();

  // 2. Biến lưu ID Bác sĩ (Nếu là Doctor)
  public currentDoctorIdSubject = new BehaviorSubject<number | null>(null);
  public currentDoctorId$ = this.currentDoctorIdSubject.asObservable();

  // 3. (MỚI) Biến lưu ID Lễ tân (Nếu là Receptionist)
  public currentReceptionistIdSubject = new BehaviorSubject<number | null>(null);
  public currentReceptionistId$ = this.currentReceptionistIdSubject.asObservable();

  constructor(
    private http: HttpClient,
    private userService: UserService
  ) {}

  // --- GETTERS TIỆN ÍCH ---
  get isAdmin(): boolean { return this.hasRole(['ADMIN']); }
  get isDoctor(): boolean { return this.hasRole(['DOCTOR']); }
  get isReceptionist(): boolean { return this.hasRole(['RECEPTIONIST']); }

  // --- LOGIN LOGIC ---
  login(request: AuthenticationRequest): Observable<ApiResponse<AuthenticationResponse>> {
    return this.http.post<ApiResponse<AuthenticationResponse>>(`${this.authUrl}/login`, request)
      .pipe(
        switchMap(response => {
          if (response.result?.token) {
            localStorage.setItem('token', response.result.token);
            return this.fetchProfile().pipe(map(() => response));
          }
          return of(response);
        })
      );
  }

  // --- APP INITIALIZER ---
  initializeUser(): Promise<any> {
    const token = localStorage.getItem('token');
    if (!token) return Promise.resolve();

    return lastValueFrom(
      this.userService.getMyInfo().pipe(
        tap(res => this.handleUserResponse(res.result)),
        catchError(() => {
          this.logout();
          return of(null);
        })
      )
    );
  }

  fetchProfile(): Observable<UserResponse | null> {
    return this.userService.getMyInfo().pipe(
      map(res => {
        this.handleUserResponse(res.result);
        return res.result || null;
      }),
      catchError(() => {
        this.logout();
        return of(null);
      })
    );
  }

  // --- XỬ LÝ DỮ LIỆU USER CHUNG ---
  private handleUserResponse(user: UserResponse | undefined) {
    if (user) {
      this.currentUserSubject.next(user);

      if (user.roles) {
        const roleNames = user.roles.map(r => r.name);
        localStorage.setItem('user_roles', JSON.stringify(roleNames));
      }

      // 1. Tự động lấy ID Bác sĩ
      if (this.isDoctor) {
        this.fetchDoctorId(user.userId);
      } else {
        this.currentDoctorIdSubject.next(null);
      }

      // 2. (MỚI) Tự động lấy ID Lễ tân
      if (this.isReceptionist) {
        this.fetchReceptionistId(user.userId);
      } else {
        this.currentReceptionistIdSubject.next(null);
      }
    }
  }

  // API lấy thông tin Bác sĩ
  private fetchDoctorId(userId: number) {
    this.http.get<any>(`${environment.apiUrl}/doctors/find-by-user/${userId}`)
      .subscribe({
        next: (res) => {
          if (res.result) this.currentDoctorIdSubject.next(res.result.doctorId);
        },
        error: () => console.warn('Không thể lấy thông tin bác sĩ')
      });
  }

  // (MỚI) API lấy thông tin Lễ tân
  private fetchReceptionistId(userId: number) {
    // Lưu ý: Bạn cần đảm bảo Backend đã có API này (tương tự Doctor)
    this.http.get<any>(`${environment.apiUrl}/receptionists/find-by-user/${userId}`)
      .subscribe({
        next: (res) => {
          if (res.result) this.currentReceptionistIdSubject.next(res.result.receptionistId);
        },
        error: () => console.warn('Không thể lấy thông tin lễ tân')
      });
  }

  // --- CHECK ROLE ---
  hasRole(requiredRoles: string[]): boolean {
    const user = this.currentUserSubject.value;
    if (user && user.roles) {
      return user.roles.some(roleObj => requiredRoles.includes(roleObj.name));
    }
    const storedRoles = localStorage.getItem('user_roles');
    if (storedRoles) {
      try {
        const roles: string[] = JSON.parse(storedRoles);
        return requiredRoles.some(role => roles.includes(role));
      } catch (e) { return false; }
    }
    return false;
  }

  // --- LOGOUT ---
  logout(): Observable<ApiResponse<void>> {
    const token = localStorage.getItem('token') || '';
    localStorage.removeItem('token');
    localStorage.removeItem('user_roles');

    this.currentUserSubject.next(null);
    this.currentDoctorIdSubject.next(null);      // Reset ID Bác sĩ
    this.currentReceptionistIdSubject.next(null); // Reset ID Lễ tân

    return this.http.post<ApiResponse<void>>(`${this.authUrl}/logout`, { token });
  }

  refreshToken(token: string): Observable<ApiResponse<AuthenticationResponse>> {
    return this.http.post<ApiResponse<AuthenticationResponse>>(`${this.authUrl}/refresh`, { token });
  }

  introspect(token: string): Observable<ApiResponse<IntrospectResponse>> {
    return this.http.post<ApiResponse<IntrospectResponse>>(`${this.authUrl}/introspect`, { token });
  }
}
