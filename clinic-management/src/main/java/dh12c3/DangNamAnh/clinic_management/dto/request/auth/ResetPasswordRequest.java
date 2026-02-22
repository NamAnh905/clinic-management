package dh12c3.DangNamAnh.clinic_management.dto.request.auth;
public record ResetPasswordRequest(String email, String otp, String newPassword) {}