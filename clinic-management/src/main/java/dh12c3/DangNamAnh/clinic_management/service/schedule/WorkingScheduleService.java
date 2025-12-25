package dh12c3.DangNamAnh.clinic_management.service.schedule;

import dh12c3.DangNamAnh.clinic_management.dto.request.schedule.ScheduleCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.schedule.ScheduleUpdationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.schedule.ScheduleResponse;
import dh12c3.DangNamAnh.clinic_management.entity.schedule.WorkingSchedule;
import dh12c3.DangNamAnh.clinic_management.entity.staff.Doctor;
import dh12c3.DangNamAnh.clinic_management.entity.staff.Receptionist;
import dh12c3.DangNamAnh.clinic_management.enums.AppointmentStatus;
import dh12c3.DangNamAnh.clinic_management.exception.AppException;
import dh12c3.DangNamAnh.clinic_management.exception.ErrorCode;
import dh12c3.DangNamAnh.clinic_management.mapper.schedule.WorkingScheduleMapper;
import dh12c3.DangNamAnh.clinic_management.repository.appoinment.AppointmentRepository;
import dh12c3.DangNamAnh.clinic_management.repository.schedule.WorkingScheduleRepository;
import dh12c3.DangNamAnh.clinic_management.repository.staff.DoctorRepository;
import dh12c3.DangNamAnh.clinic_management.repository.staff.ReceptionistRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)

public class WorkingScheduleService {
    WorkingScheduleRepository workingScheduleRepository;
    WorkingScheduleMapper workingScheduleMapper;
    DoctorRepository doctorRepository;
    ReceptionistRepository receptionistRepository;
    AppointmentRepository appointmentRepository;

    @Transactional
    public ScheduleResponse createSchedule(ScheduleCreationRequest request) {
        WorkingSchedule workingSchedule = workingScheduleMapper.toSchedule(request);

        Long doctorId = request.getDoctorId();
        Long receptionistId = request.getReceptionistId();

        if (doctorId != null){
            Doctor doctor = doctorRepository.findById(doctorId)
                    .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
            workingSchedule.setDoctor(doctor);
        }
        else if (receptionistId != null){
            Receptionist receptionist = receptionistRepository.findById(receptionistId)
                    .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));
            workingSchedule.setReceptionist(receptionist);
        }

        Long isOverLap = workingScheduleRepository.existsByOverLap(
                doctorId,
                receptionistId,
                request.getWorkDate(),
                request.getStartTime(),
                request.getEndTime()
        );

        if (isOverLap > 0) {
            throw new AppException(ErrorCode.EXISTED_SCHEDULE);
        }

        WorkingSchedule saved = workingScheduleRepository.save(workingSchedule);

        return workingScheduleMapper.toScheduleResponse(saved);
    }

    @Transactional
    public ScheduleResponse update(ScheduleUpdationRequest request, Long scheduleId) {
        WorkingSchedule workingSchedule = workingScheduleRepository.findById(scheduleId)
                .orElseThrow(()-> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (workingSchedule.getDoctor() != null) {
            checkIfScheduleHasAppointments(workingSchedule);
        }

        Long doctorId = workingSchedule.getDoctor() != null ? workingSchedule.getDoctor().getDoctorId() : null;
        Long receptionistId = workingSchedule.getReceptionist() != null ? workingSchedule.getReceptionist().getReceptionistId() : null;

        Long isOverLap = workingScheduleRepository.existsByOverLapForUpdate(
                doctorId,
                receptionistId,
                request.getWorkDate(),
                request.getStartTime(),
                request.getEndTime(),
                scheduleId
        );

        if (isOverLap > 0) {
            throw new AppException(ErrorCode.EXISTED_SCHEDULE);
        }

        workingScheduleMapper.updateSchedule(request, workingSchedule);
        var saved = workingScheduleRepository.save(workingSchedule);

        return workingScheduleMapper.toScheduleResponse(saved);
    }

    public PageResponse<ScheduleResponse> findAllSchedules(int page,
                                                           int size,
                                                           Long doctorId,
                                                           Long receptionistId,
                                                           Long specialtyId,
                                                           LocalDate startDate,
                                                           LocalDate endDate,
                                                           String viewType
    ) {
        Sort sort = Sort.by(Sort.Direction.DESC, "workDate");
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<WorkingSchedule> workingSchedules = workingScheduleRepository
                .getAllWorkingSchedules(doctorId, receptionistId, specialtyId, startDate, endDate, viewType, pageable);

        return workingScheduleMapper.toPageResponse(workingSchedules);
    }

    public ScheduleResponse findScheduleById(Long scheduleId) {
        WorkingSchedule workingSchedule = workingScheduleRepository.findById(scheduleId)
                .orElseThrow(()-> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        return workingScheduleMapper.toScheduleResponse(workingSchedule);
    }

    @Transactional
    public void deleteScheduleById(Long scheduleId) {
        WorkingSchedule workingSchedule = workingScheduleRepository.findById(scheduleId)
                .orElseThrow(()-> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));

        if (workingSchedule.getDoctor() != null) {
            checkIfScheduleHasAppointments(workingSchedule);
        }
        workingSchedule.setDeleted(true);
        workingScheduleRepository.save(workingSchedule);
    }

    private void checkIfScheduleHasAppointments(WorkingSchedule schedule) {
        LocalDateTime startDateTime = LocalDateTime.of(schedule.getWorkDate(), schedule.getStartTime());
        LocalDateTime endDateTime = LocalDateTime.of(schedule.getWorkDate(), schedule.getEndTime());

        List<AppointmentStatus> activeStatuses = List.of(
                AppointmentStatus.PENDING,
                AppointmentStatus.CONFIRMED
        );

        boolean hasAppointment = appointmentRepository.existsActiveAppointment(
                schedule.getDoctor().getDoctorId(),
                startDateTime,
                endDateTime,
                activeStatuses
        );

        if (hasAppointment) {
            throw new AppException(ErrorCode.CANNOT_CHANGE_SCHEDULE);
        }
    }
}
