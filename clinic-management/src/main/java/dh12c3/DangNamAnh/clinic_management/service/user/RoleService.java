package dh12c3.DangNamAnh.clinic_management.service.user;

import dh12c3.DangNamAnh.clinic_management.dto.request.user.RoleRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.user.RoleResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.user.RoleWithPermissionsResponse;
import dh12c3.DangNamAnh.clinic_management.exception.AppException;
import dh12c3.DangNamAnh.clinic_management.exception.ErrorCode;
import dh12c3.DangNamAnh.clinic_management.mapper.user.RoleMapper;
import dh12c3.DangNamAnh.clinic_management.repository.user.PermissionRepository;
import dh12c3.DangNamAnh.clinic_management.repository.user.RoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleService {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RoleMapper roleMapper;

    public RoleResponse create(RoleRequest request) {
        var role = roleMapper.toRole(request);

        var permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions));

        role = roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    public List<RoleResponse> getAll() {
        return roleRepository.findAll().stream().map(roleMapper::toRoleResponse).toList();
    }

    public RoleWithPermissionsResponse getById(String name) {
        var role = roleRepository.findById(name)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_KEY));
        return roleMapper.toRoleWithPermissionsResponse(role);
    }

    public void delete(String role) {
        roleRepository.deleteById(role);
    }
}
