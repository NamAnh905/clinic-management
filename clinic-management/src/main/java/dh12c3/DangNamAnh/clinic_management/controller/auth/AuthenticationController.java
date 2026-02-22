package dh12c3.DangNamAnh.clinic_management.controller.auth;

import java.text.ParseException;

import dh12c3.DangNamAnh.clinic_management.dto.request.auth.*;
import dh12c3.DangNamAnh.clinic_management.dto.response.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dh12c3.DangNamAnh.clinic_management.dto.response.auth.AuthenticationResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.auth.IntrospectResponse;
import dh12c3.DangNamAnh.clinic_management.service.auth.AuthenticationService;
import com.nimbusds.jose.JOSEException;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/login")
    ApiResponse<AuthenticationResponse> login(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> authenticate(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder().build();
    }

    @PostMapping("/forgot-password")
    ApiResponse<Void> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authenticationService.sendOtp(request);
        return ApiResponse.<Void>builder().message("OTP đã được gửi thành công").build();
    }

    @PostMapping("/reset-password")
    ApiResponse<Void> resetPassword(@RequestBody ResetPasswordRequest request) {
        authenticationService.resetPassword(request);
        return ApiResponse.<Void>builder().message("Đổi mật khẩu thành công").build();
    }
}