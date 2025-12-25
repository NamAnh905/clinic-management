package dh12c3.DangNamAnh.clinic_management.mapper.medical;

import dh12c3.DangNamAnh.clinic_management.dto.request.medical.PresDetailCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.medical.PresDetailUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.medical.PresDetailResponse;
import dh12c3.DangNamAnh.clinic_management.entity.medical.PrescriptionDetail;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        uses = {PrescriptionMapper.class})
public interface PresDetailMapper {
    @Mapping(target = "prescription",  ignore = true)
    @Mapping(target = "drug",  ignore = true)
    PrescriptionDetail toPrescriptionDetail(PresDetailCreationRequest request);

//    @Mapping(target = "prescriptionId", source = "prescription.prescriptionId")
//    @Mapping(target = "drugId",  source = "drug.drugId")
    @Mapping(target = "drugName", source = "drug.name")
    @Mapping(target = "unit", source = "drug.unit")
    PresDetailResponse toPresDetailResponse(PrescriptionDetail prescriptionDetail);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "prescription",  ignore = true)
    @Mapping(target = "drug",  ignore = true)
    void update(PresDetailUpdateRequest request, @MappingTarget PrescriptionDetail prescriptionDetail);
}
