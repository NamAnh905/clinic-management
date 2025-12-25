package dh12c3.DangNamAnh.clinic_management.mapper.patient;

import dh12c3.DangNamAnh.clinic_management.dto.request.patient.PatientCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.patient.PatientUpdationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.patient.PatientResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.schedule.ScheduleResponse;
import dh12c3.DangNamAnh.clinic_management.entity.patient.Patient;
import dh12c3.DangNamAnh.clinic_management.entity.schedule.WorkingSchedule;
import dh12c3.DangNamAnh.clinic_management.mapper.appointment.AppointmentMapper;
import dh12c3.DangNamAnh.clinic_management.mapper.schedule.WorkingScheduleMapper;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring",  uses = {AppointmentMapper.class})
public interface PatientMapper {
    @Mapping(target = "user", ignore = true)
    Patient toPatient(PatientCreationRequest request);

    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "patientName", source = "user.fullName")
    @Mapping(target = "gender", source = "user.gender")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "phoneNumber", source = "user.phoneNumber")
    @Mapping(target = "address", source = "user.address")
    @Mapping(target = "dateOfBirth", source = "user.dateOfBirth")
    PatientResponse toPatientResponse(Patient patient);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", ignore = true)
    void update(PatientUpdationRequest request, @MappingTarget Patient patient);

    default PageResponse<PatientResponse> toPageResponse(Page<Patient> page) {
        if (page == null) {
            return null;
        }

        return PageResponse.<PatientResponse>builder()
                .currentPage(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .data(page.getContent().stream()
                        .map(this::toPatientResponse)
                        .toList())
                .build();
    }
}
