package dh12c3.DangNamAnh.clinic_management.mapper.schedule;

import dh12c3.DangNamAnh.clinic_management.dto.request.schedule.ScheduleCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.schedule.ScheduleUpdationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.schedule.ScheduleResponse;
import dh12c3.DangNamAnh.clinic_management.entity.schedule.WorkingSchedule;
import dh12c3.DangNamAnh.clinic_management.repository.master.SpecialtyRepository;
import dh12c3.DangNamAnh.clinic_management.repository.staff.DoctorRepository;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring", uses = {SpecialtyRepository.class, DoctorRepository.class})
public interface WorkingScheduleMapper {

    @Mapping(target = "scheduleId", ignore = true)
    WorkingSchedule toSchedule(ScheduleCreationRequest request);

    @Mapping(target = "doctorName", source = "doctor.user.fullName")
    @Mapping(target = "doctorId", source = "doctor.doctorId")
    @Mapping(target = "receptionistId", source = "receptionist.receptionistId")
    @Mapping(target = "receptionistName", source = "receptionist.user.fullName")
    @Mapping(target = "specialty", source = "doctor.specialty.name")
    @Mapping(target = "specialtyId", source = "doctor.specialty.specialtyId")
    ScheduleResponse toScheduleResponse(WorkingSchedule workingSchedule);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "scheduleId", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    void updateSchedule(ScheduleUpdationRequest request,  @MappingTarget WorkingSchedule workingSchedule);

    default PageResponse<ScheduleResponse> toPageResponse(Page<WorkingSchedule> page) {
        if (page == null) {
            return null;
        }

        return PageResponse.<ScheduleResponse>builder()
                .currentPage(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .data(page.getContent().stream()
                        .map(this::toScheduleResponse)
                        .toList())
                .build();
    }
}
