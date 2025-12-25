package dh12c3.DangNamAnh.clinic_management.mapper.appointment;

import dh12c3.DangNamAnh.clinic_management.dto.request.appointment.AppointmentCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.appointment.AppointmentUpdationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.appointment.AppointmentResponse;
import dh12c3.DangNamAnh.clinic_management.entity.appointment.Appointment;
import dh12c3.DangNamAnh.clinic_management.mapper.patient.PatientMapper;
import dh12c3.DangNamAnh.clinic_management.mapper.staff.DoctorMapper;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

@Mapper(
        componentModel = "spring",
        uses = { PatientMapper.class, DoctorMapper.class }
)

public interface AppointmentMapper {
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    Appointment toAppointment(AppointmentCreationRequest request);

    @Mapping(target = "patientId", source = "patient.patientId")
    @Mapping(target = "patientName", source = "patient.user.fullName")
    @Mapping(target = "doctorId", source = "doctor.doctorId")
    @Mapping(target = "doctorName", source = "doctor.user.fullName")
    AppointmentResponse toAppointmentResponse(Appointment appointment);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "patient", ignore = true)
    @Mapping(target = "doctor", ignore = true)
    void update(AppointmentUpdationRequest request, @MappingTarget Appointment appointment);

    default PageResponse<AppointmentResponse> toPageResponse(Page<Appointment> page) {
        if (page == null) {
            return null;
        }

        return PageResponse.<AppointmentResponse>builder()
                .currentPage(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .data(page.getContent().stream()
                        .map(this::toAppointmentResponse)
                        .toList())
                .build();
    }
}
