package dh12c3.DangNamAnh.clinic_management.service.user;

import dh12c3.DangNamAnh.clinic_management.dto.request.user.PermissionRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.user.PermissionResponse;
import dh12c3.DangNamAnh.clinic_management.entity.user.Permission;
import dh12c3.DangNamAnh.clinic_management.mapper.user.PermissionMapper;
import dh12c3.DangNamAnh.clinic_management.repository.user.PermissionRepository;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)

public class PermissionService {
    PermissionRepository permissionRepository;
    PermissionMapper permissionMapper;

    @Transactional
    public PermissionResponse create(PermissionRequest permissionRequest) {
        Permission permission = permissionMapper.toPermission(permissionRequest);

        permissionRepository.save(permission);

        return permissionMapper.toPermissionResponse(permission);
    }

    public List<PermissionResponse> getAll() {
        var permissions = permissionRepository.findAll();
        return permissions.stream()
                .map(permissionMapper::toPermissionResponse)
                .toList();
    }

    @Transactional
    public void delete(String permission) {
        permissionRepository.deleteById(permission);
    }
}
