package dh12c3.DangNamAnh.clinic_management.mapper.user;

import dh12c3.DangNamAnh.clinic_management.dto.request.user.UserCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.user.UserUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.user.UserResponse;
import dh12c3.DangNamAnh.clinic_management.entity.user.User;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(UserCreationRequest request);

    @Mapping(target = "isActive", source = "active")
    UserResponse toUserResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "active",  ignore = true)
    void updateUser(UserUpdateRequest request, @MappingTarget User user);

    default PageResponse<UserResponse> toPageResponse(Page<User> page) {
        if (page == null) {
            return null;
        }

        return PageResponse.<UserResponse>builder()
                .currentPage(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .data(page.getContent().stream()
                        .map(this::toUserResponse)
                        .toList())
                .build();
    }
}
