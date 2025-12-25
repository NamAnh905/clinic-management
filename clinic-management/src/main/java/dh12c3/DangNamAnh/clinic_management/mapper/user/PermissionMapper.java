package dh12c3.DangNamAnh.clinic_management.mapper.user;

import dh12c3.DangNamAnh.clinic_management.dto.request.user.PermissionRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.user.PermissionResponse;
import dh12c3.DangNamAnh.clinic_management.entity.user.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest permissionRequest);
    PermissionResponse toPermissionResponse(Permission permission);
}
