package dh12c3.DangNamAnh.clinic_management.service.medical;

import dh12c3.DangNamAnh.clinic_management.component.SecurityUtils;
import dh12c3.DangNamAnh.clinic_management.dto.request.medical.MedicalRecordCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.medical.MedicalRecordUpdationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.medical.MedicalRecordResponse;
import dh12c3.DangNamAnh.clinic_management.entity.appointment.Appointment;
import dh12c3.DangNamAnh.clinic_management.entity.medical.MedicalRecord;
import dh12c3.DangNamAnh.clinic_management.entity.patient.Patient;
import dh12c3.DangNamAnh.clinic_management.enums.AppointmentStatus;
import dh12c3.DangNamAnh.clinic_management.exception.AppException;
import dh12c3.DangNamAnh.clinic_management.exception.ErrorCode;
import dh12c3.DangNamAnh.clinic_management.mapper.medical.MedicalRecordMapper;
import dh12c3.DangNamAnh.clinic_management.repository.appoinment.AppointmentRepository;
import dh12c3.DangNamAnh.clinic_management.repository.medical.MedicalRecordRepository;
import dh12c3.DangNamAnh.clinic_management.repository.patient.PatientRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)

public class MedicalRecordService {
    MedicalRecordRepository medicalRecordRepository;
    MedicalRecordMapper medicalRecordMapper;
    AppointmentRepository appointmentRepository;
    PatientRepository patientRepository;

    SecurityUtils  securityUtils;

    @Transactional
    public MedicalRecordResponse create(MedicalRecordCreationRequest request){
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        checkDoctorAuthorization(appointment);

        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new AppException(ErrorCode.STATUS_CHANGE_NOT_ALLOWED);
        }

        MedicalRecord record = medicalRecordMapper.toMedicalRecord(request);
        record.setAppointment(appointment);

        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        medicalRecordRepository.save(record);
        return medicalRecordMapper.toMedicalRecordResponse(record);
    }

    @Transactional
    public MedicalRecordResponse update(MedicalRecordUpdationRequest request, Long recordId){
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new AppException(ErrorCode.RECORD_NOT_FOUND));

        Appointment appointment = record.getAppointment();
        checkDoctorAuthorization(appointment);

        medicalRecordMapper.update(request,record);

        medicalRecordRepository.save(record);
        return medicalRecordMapper.toMedicalRecordResponse(record);
    }

    public MedicalRecordResponse findById(Long recordId){
        MedicalRecord record = medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new AppException(ErrorCode.RECORD_NOT_FOUND));

        String currentUsername = securityUtils.getCurrentUserLogin();

        boolean canSeeAll = securityUtils.hasRole("READ_ALL_PATIENT", "READ_MEDICAL_RECORD");

        if (!canSeeAll) {
            String ownerEmail = record.getAppointment().getPatient().getUser().getEmail();
            if (!ownerEmail.equals(currentUsername)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }

        return medicalRecordMapper.toMedicalRecordResponse(record);
    }

    public PageResponse<MedicalRecordResponse> findAll(Long doctorId, LocalDateTime startDate, LocalDateTime endDate, String keyword, int page, int size){
        Sort sort = Sort.by(Sort.Direction.ASC, "recordId");
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        String currentUsername = securityUtils.getCurrentUserLogin();
        boolean isAdminOrDoctor = securityUtils.hasRole("READ_MEDICAL_RECORD");
        boolean isPatient = securityUtils.hasRole("READ_OWN_MEDICAL_RECORD");

        Page<MedicalRecord> pageData;

        if (isAdminOrDoctor){
            pageData = medicalRecordRepository.getAllMedicalRecord(doctorId, startDate, endDate, keyword, pageable);
        }
        else if (isPatient){
            Patient patient = patientRepository.findByUser_Email(currentUsername)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            pageData = medicalRecordRepository.findByAppointment_Patient_PatientId(patient.getPatientId(), pageable);
        }
        else {
            pageData = Page.empty();
        }

        return medicalRecordMapper.toRecordPage(pageData);
    }

    @Transactional
    public void delete(Long recordId){
        medicalRecordRepository.deleteById(recordId);
    }

    private void checkDoctorAuthorization(Appointment appointment) {
        String currentUsername = securityUtils.getCurrentUserLogin();
        boolean isAdmin = securityUtils.hasRole();

        if (!isAdmin) {
            String doctorEmail = appointment.getDoctor().getUser().getEmail();
            if (!doctorEmail.equals(currentUsername)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }
    }
}
