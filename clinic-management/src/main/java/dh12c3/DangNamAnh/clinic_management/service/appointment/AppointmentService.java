package dh12c3.DangNamAnh.clinic_management.service.appointment;

import dh12c3.DangNamAnh.clinic_management.component.SecurityUtils;
import dh12c3.DangNamAnh.clinic_management.dto.request.appointment.AppointmentCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.appointment.AppointmentUpdationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.appointment.AppointmentResponse;
import dh12c3.DangNamAnh.clinic_management.entity.appointment.Appointment;
import dh12c3.DangNamAnh.clinic_management.entity.patient.Patient;
import dh12c3.DangNamAnh.clinic_management.entity.staff.Doctor;
import dh12c3.DangNamAnh.clinic_management.enums.AppointmentStatus;
import dh12c3.DangNamAnh.clinic_management.exception.AppException;
import dh12c3.DangNamAnh.clinic_management.exception.ErrorCode;
import dh12c3.DangNamAnh.clinic_management.mapper.appointment.AppointmentMapper;
import dh12c3.DangNamAnh.clinic_management.repository.appoinment.AppointmentRepository;
import dh12c3.DangNamAnh.clinic_management.repository.patient.PatientRepository;
import dh12c3.DangNamAnh.clinic_management.repository.staff.DoctorRepository;
import dh12c3.DangNamAnh.clinic_management.service.ExcelExportService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
@Transactional(readOnly = true)

public class AppointmentService {
    AppointmentRepository appointmentRepository;
    AppointmentMapper appointmentMapper;
    DoctorRepository doctorRepository;
    PatientRepository patientRepository;
    ExcelExportService excelExportService;

    SecurityUtils securityUtils;

    @Transactional
    public AppointmentResponse create(AppointmentCreationRequest request){
        Doctor doctor = doctorRepository.findByUserIdWithLock(request.getDoctorId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        String currentUsername = securityUtils.getCurrentUserLogin();

        boolean isAdminOrStaff = securityUtils.hasRole("CREATE_APPOINTMENT");

        Patient patient;

        if (isAdminOrStaff){
            patient = patientRepository.findByUser_UserId(request.getPatientId())
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        }
        else {
            patient = patientRepository.findByUser_Email(currentUsername)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            if (request.getPatientId() != null && !request.getPatientId().equals(patient.getPatientId())) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }

        int APPOINTMENT_DURATION = 30;
        LocalDateTime startTime = request.getAppointmentTime();
        LocalDateTime endTime = startTime.plusMinutes(APPOINTMENT_DURATION);

        var schedules = doctor.getWorkingSchedules();
        if (schedules == null || schedules.isEmpty()) {
            throw new AppException(ErrorCode.DOCTOR_HAS_NO_WORKING_SCHEDULE);
        }

        LocalDate bookingDate = request.getAppointmentTime().toLocalDate();
        LocalTime bookingTime = request.getAppointmentTime().toLocalTime();

        boolean isValidTime = schedules.stream()
                .anyMatch(schedule -> {
                    boolean isDateMatch = schedule.getWorkDate().equals(bookingDate);

                    boolean isTimeMatch = !bookingTime.isBefore(schedule.getStartTime()) &&
                            bookingTime.isBefore(schedule.getEndTime());

                    return isDateMatch && isTimeMatch;
                });

        if (!isValidTime) {
            throw new AppException(ErrorCode.DOCTOR_HAS_NO_WORKING_SCHEDULE);
        }

        boolean isValidSchedule = schedules.stream()
                .anyMatch(schedule -> {
                    boolean isDateMatch = schedule.getWorkDate().equals(bookingDate);

                    boolean isTimeMatch = !startTime.toLocalTime().isBefore(schedule.getStartTime()) &&
                            !endTime.toLocalTime().isAfter(schedule.getEndTime());

                    return isDateMatch && isTimeMatch;
                    }
                );

        if  (!isValidSchedule) {
            throw new AppException(ErrorCode.DOCTOR_HAS_NO_WORKING_SCHEDULE);
        }

        Appointment appointment = appointmentMapper.toAppointment(request);
        appointment.setEndTime(endTime);

        List<AppointmentStatus> busyStatuses = List.of(
                AppointmentStatus.PENDING,
                AppointmentStatus.CONFIRMED,
                AppointmentStatus.COMPLETED
        );

        boolean isOverLapped = appointmentRepository.existsByOverlap(
                doctor.getDoctorId(),
                appointment.getAppointmentTime(),
                appointment.getEndTime(),
                busyStatuses
        );

        if (isOverLapped) {
            throw new AppException(ErrorCode.APPOINTMENT_ALREADY_BOOKED);
        }

        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setStatus(AppointmentStatus.PENDING);

        Appointment saved = appointmentRepository.save(appointment);
        return appointmentMapper.toAppointmentResponse(saved);
    }

    @Transactional
    public AppointmentResponse update(AppointmentUpdationRequest request, Long appointmentId){
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        AppointmentStatus oldStatus = appointment.getStatus();
        AppointmentStatus newStatus = request.getStatus();

        if (newStatus != null && !oldStatus.equals(newStatus)) {
            if (oldStatus == AppointmentStatus.COMPLETED ||
                oldStatus == AppointmentStatus.CANCELLED||
                oldStatus == AppointmentStatus.NO_SHOW) {
                    throw new AppException(ErrorCode.STATUS_CHANGE_NOT_ALLOWED);
            }

            if (oldStatus == AppointmentStatus.CONFIRMED || oldStatus == AppointmentStatus.PENDING) {
                if (newStatus == AppointmentStatus.CANCELLED) {
                    LocalDateTime appointmentTime = appointment.getAppointmentTime();

                    LocalDateTime restrictedTime = appointmentTime.minusHours(24);
                    LocalDateTime now = LocalDateTime.now();

                    if(now.isAfter(restrictedTime)) {
                        throw new AppException(ErrorCode.CANNOT_CANCEL_LATE);
                    }
                }
            }

            if (oldStatus == AppointmentStatus.PENDING) {
                if (newStatus == AppointmentStatus.COMPLETED || newStatus == AppointmentStatus.NO_SHOW) {
                    throw new AppException(ErrorCode.STATUS_CHANGE_NOT_ALLOWED);
                }
            }

            if (oldStatus == AppointmentStatus.CONFIRMED && newStatus == AppointmentStatus.PENDING) {
                throw new AppException(ErrorCode.STATUS_CHANGE_NOT_ALLOWED);
            }

            appointment.setStatus(newStatus);
        }

        appointmentMapper.update(request,appointment);

        Appointment updated = appointmentRepository.save(appointment);
        return appointmentMapper.toAppointmentResponse(updated);
    }

    public PageResponse<AppointmentResponse> findAll(Long doctorId,
                                                     Long patientId,
                                                     AppointmentStatus status,
                                                     LocalDateTime startDate,
                                                     LocalDateTime endDate,
                                                     String keyword,
                                                     int page,
                                                     int size
    ) {
        Sort sort = Sort.by(Sort.Direction.DESC, "appointmentTime");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Appointment> pageData;
        String currentUsername = securityUtils.getCurrentUserLogin();

        boolean isDoctor = securityUtils.hasRole("UPDATE_PRESCRIPTION");
        boolean isPatient = securityUtils.hasRole("READ_OWN_APPOINTMENT");
        boolean isAdminOrReceptionist = securityUtils.hasRole("READ_APPOINTMENT");

        if (isAdminOrReceptionist) {}
        else if (isDoctor) {
            Doctor doctor = doctorRepository.findByUser_Email(currentUsername)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            doctorId = doctor.getUser().getUserId();
        }
        else if (isPatient) {
            Patient patient = patientRepository.findByUser_Email(currentUsername)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            patientId = patient.getUser().getUserId();
            doctorId = null;
        }
        else {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        pageData = appointmentRepository.searchAppointments(doctorId, patientId, status, startDate, endDate, keyword, pageable);
        return appointmentMapper.toPageResponse(pageData);
    }

    public AppointmentResponse findById(Long appointmentId){
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        String currentUsername = securityUtils.getCurrentUserLogin();
        boolean isAdminOrReceptionist = securityUtils.hasRole("READ_APPOINTMENT");

        if (!isAdminOrReceptionist) {
            boolean isMyAppointment =
                    appointment.getPatient().getUser().getEmail().equals(currentUsername) ||
                    appointment.getDoctor().getUser().getEmail().equals(currentUsername);

            if (!isMyAppointment) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }

        return appointmentMapper.toAppointmentResponse(appointment);
    }

    @Transactional
    public void delete(Long appointmentId){
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));
        appointment.setDeleted(true);
        appointmentRepository.save(appointment);
    }

    public ByteArrayInputStream exportAppointments() throws IOException {
        List<Appointment> appointments = appointmentRepository.findAll(Sort.by(Sort.Direction.DESC, "appointmentTime"));

        List<AppointmentResponse> responses = appointments.stream()
                .map(appointmentMapper::toAppointmentResponse)
                .toList();

        return excelExportService.exportToExcel(responses, "Danh sách lịch hẹn");
    }
}
