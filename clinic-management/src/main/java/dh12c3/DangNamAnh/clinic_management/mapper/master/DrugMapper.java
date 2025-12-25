package dh12c3.DangNamAnh.clinic_management.mapper.master;

import dh12c3.DangNamAnh.clinic_management.dto.request.master.DrugCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.master.DrugUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.master.DrugResponse;
import dh12c3.DangNamAnh.clinic_management.entity.master.Drug;
import dh12c3.DangNamAnh.clinic_management.mapper.medical.MedicalRecordMapper;
import dh12c3.DangNamAnh.clinic_management.mapper.medical.PresDetailMapper;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", uses = {PresDetailMapper.class})
public interface DrugMapper {

    @Mapping(target = "drugId", ignore = true)
    @Mapping(target = "prescriptionDetails", ignore = true)
    Drug toDrug(DrugCreationRequest request);

    DrugResponse toDrugResponse(Drug drug);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "drugId", ignore = true)
    @Mapping(target = "prescriptionDetails", ignore = true)
    void update(DrugUpdateRequest request, @MappingTarget Drug drug);

    default PageResponse<DrugResponse> toDrugPage(Page<Drug> page) {
        if (page == null) {
            return null;
        }

        return PageResponse.<DrugResponse>builder()
                .currentPage(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .data(page.getContent().stream()
                        .map(this::toDrugResponse)
                        .toList())
                .build();
    }
}
