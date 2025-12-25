package dh12c3.DangNamAnh.clinic_management.mapper.medical;

import dh12c3.DangNamAnh.clinic_management.dto.request.medical.MedicalRecordCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.medical.MedicalRecordUpdationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.medical.MedicalRecordResponse;
import dh12c3.DangNamAnh.clinic_management.entity.medical.MedicalRecord;
import dh12c3.DangNamAnh.clinic_management.mapper.appointment.AppointmentMapper;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", uses = {AppointmentMapper.class, PrescriptionMapper.class})
public interface MedicalRecordMapper {

    @Mapping(target = "appointment", ignore = true)
    MedicalRecord toMedicalRecord(MedicalRecordCreationRequest request);

    @Mapping(target = "appointmentId", source = "appointment.appointmentId")
    @Mapping(target = "patientName", source = "appointment.patient.user.fullName")
    @Mapping(target = "visitDate", source = "appointment.appointmentTime")
    @Mapping(target = "doctorId", source = "appointment.doctor.doctorId")
    @Mapping(target = "doctorName", source = "appointment.doctor.user.fullName")
    MedicalRecordResponse toMedicalRecordResponse(MedicalRecord medicalRecord);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "appointment", ignore = true)
    void update(MedicalRecordUpdationRequest request, @MappingTarget MedicalRecord medicalRecord);

    default PageResponse<MedicalRecordResponse> toRecordPage(Page<MedicalRecord> page) {
        if (page == null) {
            return null;
        }

        return PageResponse.<MedicalRecordResponse>builder()
                .currentPage(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .data(page.getContent().stream()
                        .map(this::toMedicalRecordResponse)
                        .toList())
                .build();
    }
}
