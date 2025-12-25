package dh12c3.DangNamAnh.clinic_management.controller.user;

import dh12c3.DangNamAnh.clinic_management.dto.request.user.PermissionRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.ApiResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.user.PermissionResponse;
import dh12c3.DangNamAnh.clinic_management.exception.ErrorCode;
import dh12c3.DangNamAnh.clinic_management.service.user.PermissionService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class PermissionController {
    PermissionService permissionService;

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @PostMapping
    public ApiResponse<PermissionResponse> createPermission(@RequestBody PermissionRequest permissionRequest) {
        return ApiResponse.<PermissionResponse>builder()
                .result(permissionService.create(permissionRequest))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @GetMapping
    public ApiResponse<List<PermissionResponse>> getAllPermissions() {
        return ApiResponse.<List<PermissionResponse>>builder()
                .result(permissionService.getAll())
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @DeleteMapping("/{name}")
    public void deletePermission(@PathVariable String name) {
        permissionService.delete(name);
    }
}
