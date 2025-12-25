package dh12c3.DangNamAnh.clinic_management.mapper.medical;

import dh12c3.DangNamAnh.clinic_management.dto.request.medical.PrescriptionCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.medical.PrescriptionUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.medical.PrescriptionResponse;
import dh12c3.DangNamAnh.clinic_management.entity.medical.Prescription;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", uses = {MedicalRecordMapper.class, PresDetailMapper.class})
public interface PrescriptionMapper {

    @Mapping(target = "prescriptionId", ignore = true)
    @Mapping(target = "medicalRecord", ignore = true)
    Prescription toPrescription(PrescriptionCreationRequest request);

    @Mapping(target = "recordId", source = "medicalRecord.recordId")
    @Mapping(target = "doctorName", source = "medicalRecord.appointment.doctor.user.fullName")
    PrescriptionResponse toPrescriptionResponse(Prescription prescription);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "medicalRecord", ignore = true)
    @Mapping(target = "prescriptionDetails", ignore = true)
    void update(PrescriptionUpdateRequest request, @MappingTarget Prescription prescription);

    default PageResponse<PrescriptionResponse> toPrescriptionPage(Page<Prescription> page) {
        if (page == null) {
            return null;
        }

        return PageResponse.<PrescriptionResponse>builder()
                .currentPage(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .data(page.getContent().stream()
                        .map(this::toPrescriptionResponse)
                        .toList())
                .build();
    }
}
