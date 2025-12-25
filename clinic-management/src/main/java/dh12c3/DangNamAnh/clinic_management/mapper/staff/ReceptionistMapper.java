package dh12c3.DangNamAnh.clinic_management.mapper.staff;

import dh12c3.DangNamAnh.clinic_management.dto.request.staff.ReceptionistCreateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.staff.ReceptionistUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.staff.ReceptionistResponse;
import dh12c3.DangNamAnh.clinic_management.entity.staff.Receptionist;
import dh12c3.DangNamAnh.clinic_management.mapper.user.UserMapper;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", uses = {UserMapper.class})
public interface ReceptionistMapper {

    @Mapping(target = "user", ignore = true)
    Receptionist toReceptionist(ReceptionistCreateRequest request);

    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "phoneNumber", source = "user.phoneNumber")
    @Mapping(target = "gender", source = "user.gender")
    @Mapping(target = "dateOfBirth", source = "user.dateOfBirth")
    ReceptionistResponse toReceptionistResponse(Receptionist receptionist);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", ignore = true)
    void update(ReceptionistUpdateRequest request, @MappingTarget Receptionist receptionist);

    default PageResponse<ReceptionistResponse> toPageResponse(Page<Receptionist> page){
        if (page == null) return null;

        return PageResponse.<ReceptionistResponse>builder()
                .currentPage(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .data(page.getContent().stream()
                        .map(this::toReceptionistResponse)
                        .toList())

                .build();
    }
}
