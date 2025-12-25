package dh12c3.DangNamAnh.clinic_management.mapper.staff;

import dh12c3.DangNamAnh.clinic_management.dto.request.staff.DoctorCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.staff.DoctorUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.staff.DoctorResponse;
import dh12c3.DangNamAnh.clinic_management.entity.staff.Doctor;
import dh12c3.DangNamAnh.clinic_management.mapper.appointment.AppointmentMapper;
import dh12c3.DangNamAnh.clinic_management.mapper.schedule.WorkingScheduleMapper;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", uses = {WorkingScheduleMapper.class, AppointmentMapper.class})
public interface DoctorMapper {
    @Mapping(target = "userId", source = "user.userId")
    @Mapping(target = "specialtyId", source = "specialty.specialtyId")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "specialtyName", source = "specialty.name")
    @Mapping(target = "phoneNumber", source = "user.phoneNumber")
    DoctorResponse toDoctorResponse(Doctor doctor);

    @Mapping(target = "user", ignore = true)
    @Mapping(target = "specialty", ignore = true)
    Doctor toDoctor(DoctorCreationRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "specialty", ignore = true)
    void updateDoctor(DoctorUpdateRequest request, @MappingTarget Doctor doctor);

    default PageResponse<DoctorResponse> toDoctorResponsePage(Page<Doctor> page) {
        if (page == null) {
            return null;
        }

        return PageResponse.<DoctorResponse>builder()
                .currentPage(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .data(page.getContent().stream()
                        .map(this::toDoctorResponse)
                        .toList())
                .build();
    }
}
