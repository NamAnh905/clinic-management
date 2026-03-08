package dh12c3.DangNamAnh.clinic_management.service.appointment;

import dh12c3.DangNamAnh.clinic_management.component.SecurityUtils;
import dh12c3.DangNamAnh.clinic_management.dto.request.appointment.AppointmentCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.appointment.AppointmentUpdationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.appointment.CancelAppointmentRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.appointment.PublicAppointmentRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.appointment.AppointmentResponse;
import dh12c3.DangNamAnh.clinic_management.entity.appointment.Appointment;
import dh12c3.DangNamAnh.clinic_management.entity.patient.Patient;
import dh12c3.DangNamAnh.clinic_management.entity.staff.Doctor;
import dh12c3.DangNamAnh.clinic_management.entity.user.Role;
import dh12c3.DangNamAnh.clinic_management.entity.user.User;
import dh12c3.DangNamAnh.clinic_management.enums.AppointmentStatus;
import dh12c3.DangNamAnh.clinic_management.enums.Gender;
import dh12c3.DangNamAnh.clinic_management.exception.AppException;
import dh12c3.DangNamAnh.clinic_management.exception.ErrorCode;
import dh12c3.DangNamAnh.clinic_management.mapper.appointment.AppointmentMapper;
import dh12c3.DangNamAnh.clinic_management.mapper.user.UserMapper;
import dh12c3.DangNamAnh.clinic_management.repository.appoinment.AppointmentRepository;
import dh12c3.DangNamAnh.clinic_management.repository.patient.PatientRepository;
import dh12c3.DangNamAnh.clinic_management.repository.staff.DoctorRepository;
import dh12c3.DangNamAnh.clinic_management.repository.user.RoleRepository;
import dh12c3.DangNamAnh.clinic_management.repository.user.UserRepository;
import dh12c3.DangNamAnh.clinic_management.service.EmailService;
import dh12c3.DangNamAnh.clinic_management.service.ExcelExportService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
@Transactional(readOnly = true)

public class AppointmentService {
    AppointmentRepository appointmentRepository;
    AppointmentMapper appointmentMapper;
    DoctorRepository doctorRepository;
    PatientRepository patientRepository;
    UserRepository userRepository;
    UserMapper userMapper;
    RoleRepository roleRepository;
    PasswordEncoder passwordEncoder;
    ExcelExportService excelExportService;
    EmailService emailService;

    SecurityUtils securityUtils;

    int APPOINTMENT_DURATION = 30;

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

        LocalDateTime startTime = request.getAppointmentTime();
        LocalDateTime endTime = startTime.plusMinutes(APPOINTMENT_DURATION);

        var schedules = doctor.getWorkingSchedules();
        if (schedules == null || schedules.isEmpty()) {
            throw new AppException(ErrorCode.DOCTOR_HAS_NO_WORKING_SCHEDULE);
        }

        LocalDate bookingDate = request.getAppointmentTime().toLocalDate();

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

        boolean isPatientOverlapped = appointmentRepository.existsByPatientOverlap(
                patient.getPatientId(),
                appointment.getAppointmentTime(),
                appointment.getEndTime(),
                busyStatuses
        );

        if (isPatientOverlapped) {
            throw new AppException(ErrorCode.PATIENT_TIME_CONFLICT);
        }

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

        if (patient.getUser().getEmail() != null && !patient.getUser().getEmail().isEmpty()) {
            String timeStr = saved.getAppointmentTime().format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));
            String docName = doctor.getUser().getFullName();
            String patientName = patient.getUser().getFullName();
            String patientEmail = patient.getUser().getEmail();

            emailService.sendBookingNotification(
                    patientEmail,
                    patientName,
                    timeStr,
                    docName
            );
        }

        return appointmentMapper.toAppointmentResponse(saved);
    }

    @Transactional
    public AppointmentResponse createPublicAppointment(PublicAppointmentRequest request) {
        Doctor doctor = doctorRepository.findByUserIdWithLock(request.getDoctorId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        LocalDateTime startTime = request.getAppointmentTime();
        LocalDateTime endTime = startTime.plusMinutes(APPOINTMENT_DURATION);

        if (startTime.isBefore(LocalDateTime.now().plusMinutes(15))) {
            throw new AppException(ErrorCode.INVALID_TIME);
        }

        // 3. Danh sách các trạng thái lịch đang bận
        List<AppointmentStatus> busyStatuses = List.of(
                AppointmentStatus.PENDING,
                AppointmentStatus.CONFIRMED,
                AppointmentStatus.COMPLETED
        );

        // 4. Kiểm tra trùng lịch bác sĩ (Overlap)
        boolean isOverLapped = appointmentRepository.existsByOverlap(
                doctor.getDoctorId(),
                startTime,
                endTime,
                busyStatuses
        );

        if (isOverLapped) {
            throw new AppException(ErrorCode.APPOINTMENT_ALREADY_BOOKED);
        }

        Patient patient;
        var existingUser = userRepository.findByPhoneNumber(request.getPhoneNumber());

        String rawPassword = null;
        boolean isNewUser = false;

        // 5. Xử lý thông tin Patient/User
        if (existingUser.isPresent()) {
            patient = patientRepository.findByUser_UserId(existingUser.get().getUserId())
                    .orElseGet(() -> {
                        Patient p = new Patient();
                        p.setUser(existingUser.get());
                        return patientRepository.save(p);
                    });
        } else {
            if (request.getEmail() != null && !request.getEmail().isEmpty()) {
                if (userRepository.existsByEmail(request.getEmail())) {
                    throw new AppException(ErrorCode.EXISTED_EMAIL);
                }
            }
            User newUser = userMapper.toUserFromPublic(request);
            if (request.getEmail() == null || request.getEmail().isEmpty()) {
                newUser.setEmail(request.getPhoneNumber() + "@guest.clinic.local");
            } else {
                newUser.setEmail(request.getEmail());
            }

            rawPassword = UUID.randomUUID().toString().substring(0, 8);
            newUser.setPasswordHash(passwordEncoder.encode(rawPassword));

            try {
                newUser.setGender(Gender.valueOf(request.getGender()));
            } catch (Exception e) {
                newUser.setGender(Gender.OTHER);
            }

            Role patientRole = roleRepository.findById("PATIENT").orElse(null);
            if(patientRole != null) newUser.setRoles(Set.of(patientRole));

            userRepository.save(newUser);
            isNewUser = true;

            patient = new Patient();
            patient.setUser(newUser);
            patientRepository.save(patient);
        }

        // 6. Kiểm tra trùng lịch của chính Bệnh nhân
        if (patient.getPatientId() != null) {
            boolean isPatientOverlapped = appointmentRepository.existsByPatientOverlap(
                    patient.getPatientId(),
                    startTime,
                    endTime,
                    busyStatuses
            );

            if (isPatientOverlapped) {
                throw new AppException(ErrorCode.PATIENT_TIME_CONFLICT);
            }
        }

        // 7. Tạo lịch hẹn mới
        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setAppointmentTime(startTime);
        appointment.setEndTime(endTime);
        appointment.setStatus(AppointmentStatus.PENDING);
        appointment.setReason(request.getReason());
        appointment.setDeleted(false);

        Appointment saved = appointmentRepository.save(appointment);

        // 8. Gửi Email thông báo (Nên chuyển sang chạy @Async trong tương lai để tối ưu)
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            String timeStr = startTime.format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));
            String docName = doctor.getUser().getFullName();

            if (isNewUser && rawPassword != null) {
                emailService.sendBookingConfirmation(
                        request.getEmail(),
                        request.getFullName(),
                        timeStr,
                        docName,
                        request.getEmail(),
                        rawPassword
                );
            } else {
                emailService.sendBookingNotification(
                        request.getEmail(),
                        request.getFullName(),
                        timeStr,
                        docName
                );
            }
        }

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

                    LocalDateTime restrictedTime = appointmentTime.minusHours(4);
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
                                                     int size,
                                                     String sortBy,
                                                     String sortDir
    ) {
        String sortField = SORT_MAPPING.getOrDefault(sortBy, "appointmentTime");
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortField));

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
            patientId = patient.getPatientId();
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

    public List<String> getAvailableTimeSlots(Long doctorId, LocalDate date) {
        var doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        var schedules = doctor.getWorkingSchedules().stream()
                .filter(s -> s.getWorkDate().equals(date) && !s.isDeleted())
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.DOCTOR_HAS_NO_WORKING_SCHEDULE));

        List<LocalTime> allSlots = new ArrayList<>();
        LocalTime current = schedules.getStartTime();
        while (current.isBefore(schedules.getEndTime())) {
            allSlots.add(current);
            current = current.plusMinutes(APPOINTMENT_DURATION);
        }

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Appointment> bookedApps = appointmentRepository.findBookedAppointments(doctorId, startOfDay, endOfDay);
        List<LocalTime> bookedTimes = bookedApps.stream()
                .map(a -> a.getAppointmentTime().toLocalTime())
                .toList();
        allSlots.removeAll(bookedTimes);
        return allSlots.stream().map(LocalTime::toString).toList();
    }

    @Transactional
    public void cancelPublicAppointment(CancelAppointmentRequest request) {
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        String patientPhone = appointment.getPatient().getUser().getPhoneNumber();
        if (!patientPhone.equals(request.getPhoneNumber())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING &&
                appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new AppException(ErrorCode.STATUS_CHANGE_NOT_ALLOWED);
        }

        LocalDateTime appointmentTime = appointment.getAppointmentTime();
        LocalDateTime restrictedTime = appointmentTime.minusHours(4);
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(restrictedTime)) {
            throw new AppException(ErrorCode.CANNOT_CANCEL_LATE);
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    @Transactional
    public void cancelMyAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        // 1. Kiểm tra xem user đang đăng nhập có đúng là chủ của lịch hẹn này không
        String currentUsername = securityUtils.getCurrentUserLogin();
        if (!appointment.getPatient().getUser().getEmail().equals(currentUsername)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // 2. Chỉ cho phép hủy nếu đang ở trạng thái PENDING hoặc CONFIRMED
        if (appointment.getStatus() != AppointmentStatus.PENDING &&
                appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new AppException(ErrorCode.STATUS_CHANGE_NOT_ALLOWED);
        }

        // 3. Ràng buộc thời gian: Không cho phép hủy sát giờ (trước 4 tiếng)
        LocalDateTime appointmentTime = appointment.getAppointmentTime();
        LocalDateTime restrictedTime = appointmentTime.minusHours(4);
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(restrictedTime)) {
            throw new AppException(ErrorCode.CANNOT_CANCEL_LATE);
        }

        // 4. Thực hiện hủy
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    Map<String, String> SORT_MAPPING = Map.of(
        "patientName", "patient.user.fullName",
        "doctorName", "doctor.user.fullName",
        "appointmentTime", "appointmentTime"
    );
}
