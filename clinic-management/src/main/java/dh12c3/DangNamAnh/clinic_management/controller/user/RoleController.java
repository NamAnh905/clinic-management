package dh12c3.DangNamAnh.clinic_management.controller.user;

import dh12c3.DangNamAnh.clinic_management.dto.request.user.RoleRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.ApiResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.user.RoleResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.user.RoleWithPermissionsResponse;
import dh12c3.DangNamAnh.clinic_management.service.user.RoleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class RoleController {
    RoleService roleService;

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @PostMapping
    ApiResponse<RoleResponse> create(@RequestBody RoleRequest request) {
        return ApiResponse.<RoleResponse>builder()
                .result(roleService.create(request))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @GetMapping
    ApiResponse<List<RoleResponse>> getAll() {
        return ApiResponse.<List<RoleResponse>>builder()
                .result(roleService.getAll())
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @GetMapping("/{name}")
    ApiResponse<RoleWithPermissionsResponse>  getById(@PathVariable String name) {
        return  ApiResponse.<RoleWithPermissionsResponse>builder()
                .result(roleService.getById(name))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @DeleteMapping("/{role}")
    ApiResponse<Void> delete(@PathVariable String role) {
        roleService.delete(role);
        return ApiResponse.<Void>builder().build();
    }
}
