package dh12c3.DangNamAnh.clinic_management.mapper.master;

import dh12c3.DangNamAnh.clinic_management.dto.request.master.SpecialtyCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.master.SpecialtyUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.master.SpecialtyResponse;
import dh12c3.DangNamAnh.clinic_management.entity.master.Specialty;
import dh12c3.DangNamAnh.clinic_management.mapper.staff.DoctorMapper;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", uses = {DoctorMapper.class})
public interface SpecialtyMapper {

    @Mapping(target = "specialtyId", ignore = true)
    @Mapping(target = "doctors", ignore = true)
    Specialty toSpecialty(SpecialtyCreationRequest request);

    SpecialtyResponse toSpecialtyResponse(Specialty specialty);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "specialtyId", ignore = true)
    @Mapping(target = "doctors", ignore = true)
    void toUpdate(SpecialtyUpdateRequest request, @MappingTarget Specialty specialty);

    default PageResponse<SpecialtyResponse> toSpecialtyPage(Page<Specialty> page) {
        if (page == null) {
            return null;
        }

        return PageResponse.<SpecialtyResponse>builder()
                .currentPage(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .data(page.getContent().stream()
                        .map(this::toSpecialtyResponse)
                        .toList())
                .build();
    }
}
