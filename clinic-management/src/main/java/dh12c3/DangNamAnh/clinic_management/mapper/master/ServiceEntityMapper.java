package dh12c3.DangNamAnh.clinic_management.mapper.master;

import dh12c3.DangNamAnh.clinic_management.dto.request.master.SECreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.master.SEUpdationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.master.ServiceEntityResponse;
import dh12c3.DangNamAnh.clinic_management.entity.master.ServiceEntity;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface ServiceEntityMapper {
    ServiceEntity toEntity(SECreationRequest request);

    @Mapping(target = "type", source = "type")
    ServiceEntityResponse toResponseEntity(ServiceEntity serviceEntity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void update(SEUpdationRequest request, @MappingTarget ServiceEntity serviceEntity);

    default PageResponse<ServiceEntityResponse> toServicePage(Page<ServiceEntity> page) {
        if (page == null) {
            return null;
        }

        return PageResponse.<ServiceEntityResponse>builder()
                .currentPage(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .data(page.getContent().stream()
                        .map(this::toResponseEntity)
                        .toList())
                .build();
    }
}
