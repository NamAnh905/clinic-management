package dh12c3.DangNamAnh.clinic_management.service.patient;

import dh12c3.DangNamAnh.clinic_management.dto.request.patient.PatientCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.patient.PatientUpdationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.patient.PatientResponse;
import dh12c3.DangNamAnh.clinic_management.entity.patient.Patient;
import dh12c3.DangNamAnh.clinic_management.entity.user.User;
import dh12c3.DangNamAnh.clinic_management.exception.AppException;
import dh12c3.DangNamAnh.clinic_management.exception.ErrorCode;
import dh12c3.DangNamAnh.clinic_management.mapper.patient.PatientMapper;
import dh12c3.DangNamAnh.clinic_management.repository.patient.PatientRepository;
import dh12c3.DangNamAnh.clinic_management.repository.user.UserRepository;
import dh12c3.DangNamAnh.clinic_management.service.user.UserService;
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

public class PatientService {
    PatientRepository patientRepository;
    PatientMapper patientMapper;
    UserRepository userRepository;
    UserService userService;

    @Transactional
    public PatientResponse create(PatientCreationRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));

        boolean isPatientRole = user.getRoles().stream()
                .anyMatch(role -> "PATIENT".equals(role.getName()));

        if (!isPatientRole) {
            throw new AppException(ErrorCode.DATA_INVALID);
        }

        Patient patient = patientMapper.toPatient(request);

        patient.setUser(user);

        Patient savedPatient = patientRepository.save(patient);
        return patientMapper.toPatientResponse(savedPatient);
    }

    @Transactional
    public PatientResponse update(PatientUpdationRequest request, Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));

        User user = patient.getUser();

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getGender() != null) user.setGender(request.getGender());
        if (request.getDateOfBirth() != null) user.setDateOfBirth(request.getDateOfBirth());

        patientMapper.update(request, patient);

        Patient saved = patientRepository.save(patient);
        return patientMapper.toPatientResponse(saved);
    }

    public PageResponse<PatientResponse> findAll(String keyword, int page, int size){
        Sort sort = Sort.by(Sort.Direction.DESC, "patientId");
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<Patient> patients = patientRepository.searchPatients(keyword, pageable);

        return patientMapper.toPageResponse(patients);
    }

    public PatientResponse findById(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));

        return patientMapper.toPatientResponse(patient);
    }

    @Transactional
    public void delete(Long patientId) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(()-> new AppException(ErrorCode.USER_NOT_FOUND));

        userService.delete(patient.getUser().getUserId());
    }
}
