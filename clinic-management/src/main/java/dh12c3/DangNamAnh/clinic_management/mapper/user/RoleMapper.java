package dh12c3.DangNamAnh.clinic_management.mapper.user;

import dh12c3.DangNamAnh.clinic_management.dto.request.user.RoleRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.user.RoleResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.user.RoleWithPermissionsResponse;
import dh12c3.DangNamAnh.clinic_management.entity.user.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);

    RoleWithPermissionsResponse toRoleWithPermissionsResponse(Role role);
}