package dh12c3.DangNamAnh.clinic_management.service.medical;

import dh12c3.DangNamAnh.clinic_management.component.SecurityUtils;
import dh12c3.DangNamAnh.clinic_management.dto.request.medical.PrescriptionCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.medical.PrescriptionUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.medical.PrescriptionResponse;
import dh12c3.DangNamAnh.clinic_management.entity.medical.MedicalRecord;
import dh12c3.DangNamAnh.clinic_management.entity.medical.Prescription;
import dh12c3.DangNamAnh.clinic_management.entity.patient.Patient;
import dh12c3.DangNamAnh.clinic_management.entity.staff.Doctor;
import dh12c3.DangNamAnh.clinic_management.exception.AppException;
import dh12c3.DangNamAnh.clinic_management.exception.ErrorCode;
import dh12c3.DangNamAnh.clinic_management.mapper.medical.PrescriptionMapper;
import dh12c3.DangNamAnh.clinic_management.repository.medical.MedicalRecordRepository;
import dh12c3.DangNamAnh.clinic_management.repository.medical.PrescriptionRepository;
import dh12c3.DangNamAnh.clinic_management.repository.patient.PatientRepository;
import dh12c3.DangNamAnh.clinic_management.repository.staff.DoctorRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)

public class PrescriptionService {

    PrescriptionRepository prescriptionRepository;
    MedicalRecordRepository medicalRecordRepository;
    PrescriptionMapper prescriptionMapper;
    PatientRepository patientRepository;
    DoctorRepository doctorRepository;

    SecurityUtils securityUtils;

    @Transactional
    public PrescriptionResponse create(PrescriptionCreationRequest request) {
        MedicalRecord medicalRecord = medicalRecordRepository.findById(request.getRecordId())
                    .orElseThrow(() -> new AppException(ErrorCode.RECORD_NOT_FOUND));

        checkDoctorAuthorization(medicalRecord);

        Prescription prescription = prescriptionMapper.toPrescription(request);
        prescription.setMedicalRecord(medicalRecord);

        Prescription saved = prescriptionRepository.save(prescription);
        return prescriptionMapper.toPrescriptionResponse(saved);
    }

    @Transactional
    public PrescriptionResponse update(PrescriptionUpdateRequest request, Long prescriptionId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.PRESCRIPTION_NOT_FOUND));

        MedicalRecord record = prescription.getMedicalRecord();
        checkDoctorAuthorization(record);

        prescriptionMapper.update(request, prescription);

        Prescription saved = prescriptionRepository.save(prescription);
        return prescriptionMapper.toPrescriptionResponse(saved);
    }

    public PageResponse<PrescriptionResponse> findAll(int page, int size) {
        Sort sort = Sort.by(Sort.Direction.ASC, "prescriptionId");
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        String currentUsername = securityUtils.getCurrentUserLogin();
        boolean isAdmin = securityUtils.hasRole("FULL_ACCESS");
        boolean isDoctor = securityUtils.hasRole("CREATE_PRESCRIPTION");
        boolean isPatient = securityUtils.hasRole("READ_OWN_PRESCRIPTION");

        Page<Prescription> pageData;
        Long doctorId = null;

        if (isAdmin) {
            pageData = prescriptionRepository.getAllPrescription(null, pageable);
        }
        else if (isDoctor) {
            Doctor doctor = doctorRepository.findByUser_Email(currentUsername)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            doctorId = doctor.getDoctorId();

            pageData = prescriptionRepository.getAllPrescription(doctorId, pageable);
        }
        else if (isPatient) {
            Patient patient = patientRepository.findByUser_Email(currentUsername)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
            pageData = prescriptionRepository.findByMedicalRecord_Appointment_Patient_PatientId(patient.getPatientId(), pageable);
        }
        else {
            pageData = Page.empty();
        }

        return prescriptionMapper.toPrescriptionPage(pageData);
    }

    public PrescriptionResponse findById(Long prescriptionId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new AppException(ErrorCode.PRESCRIPTION_NOT_FOUND));

        String currentUsername = securityUtils.getCurrentUserLogin();

        boolean canSeeAll = securityUtils.hasRole("READ_PRESCRIPTION");

        if (!canSeeAll) {
            String ownerEmail = prescription.getMedicalRecord().getAppointment().getPatient().getUser().getEmail();
            if (!ownerEmail.equals(currentUsername)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }

        return prescriptionMapper.toPrescriptionResponse(prescription);
    }

    public PrescriptionResponse findByRecordId(Long recordId) {
        Prescription prescription = prescriptionRepository.findByMedicalRecord_RecordId(recordId)
                .orElse(null);

        if (prescription == null) return null;

        String currentUsername = securityUtils.getCurrentUserLogin();
        boolean canSeeAll = securityUtils.hasRole("READ_PRESCRIPTION");

        if (!canSeeAll) {
            String ownerEmail = prescription.getMedicalRecord().getAppointment().getPatient().getUser().getEmail();
            if (!ownerEmail.equals(currentUsername)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }
        return prescriptionMapper.toPrescriptionResponse(prescription);
    }

    @Transactional
    public void delete(Long prescriptionId) {
        prescriptionRepository.deleteById(prescriptionId);
    }

    private void checkDoctorAuthorization(MedicalRecord medicalRecord) {
        String currentUsername = securityUtils.getCurrentUserLogin();
        boolean isAdmin = securityUtils.hasRole();

        if (!isAdmin) {
            String doctorEmail = medicalRecord.getAppointment().getDoctor().getUser().getEmail();
            if (!doctorEmail.equals(currentUsername)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }
    }
}
