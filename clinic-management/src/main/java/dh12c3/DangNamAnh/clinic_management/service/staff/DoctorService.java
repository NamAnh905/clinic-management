package dh12c3.DangNamAnh.clinic_management.service.staff;

import dh12c3.DangNamAnh.clinic_management.dto.request.staff.DoctorCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.staff.DoctorUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.staff.DoctorResponse;
import dh12c3.DangNamAnh.clinic_management.entity.master.Specialty;
import dh12c3.DangNamAnh.clinic_management.entity.patient.Patient;
import dh12c3.DangNamAnh.clinic_management.entity.staff.Doctor;
import dh12c3.DangNamAnh.clinic_management.entity.user.Role;
import dh12c3.DangNamAnh.clinic_management.entity.user.User;
import dh12c3.DangNamAnh.clinic_management.exception.AppException;
import dh12c3.DangNamAnh.clinic_management.exception.ErrorCode;
import dh12c3.DangNamAnh.clinic_management.helper.AppUtils;
import dh12c3.DangNamAnh.clinic_management.mapper.staff.DoctorMapper;
import dh12c3.DangNamAnh.clinic_management.repository.appoinment.AppointmentRepository;
import dh12c3.DangNamAnh.clinic_management.repository.master.SpecialtyRepository;
import dh12c3.DangNamAnh.clinic_management.repository.patient.PatientRepository;
import dh12c3.DangNamAnh.clinic_management.repository.staff.DoctorRepository;
import dh12c3.DangNamAnh.clinic_management.repository.user.RoleRepository;
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

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)

public class DoctorService {
    DoctorMapper doctorMapper;
    DoctorRepository doctorRepository;
    UserRepository userRepository;
    SpecialtyRepository specialtyRepository;
    RoleRepository roleRepository;
    PatientRepository patientRepository;
    AppointmentRepository appointmentRepository;
    UserService userService;

    @Transactional
    public DoctorResponse create(DoctorCreationRequest request){
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(()->new AppException(ErrorCode.USER_NOT_FOUND));

        Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                .orElseThrow(()->new AppException(ErrorCode.SPECIALTY_NOT_FOUND));

        Role doctorRole = roleRepository.findById("DOCTOR")
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        var patientOpt = patientRepository.findByUser_UserId(request.getUserId());

        if (patientOpt.isPresent()){
            Patient patient = patientOpt.get();

            boolean hasHistory = appointmentRepository.existsByPatient_PatientId(patient.getPatientId());
            if (!hasHistory){
                user.setPatient(null);
                patientRepository.delete(patient);
            }
        }

        user.setRoles(new HashSet<>(Set.of(doctorRole)));
        userRepository.save(user);

        Doctor doctor = doctorMapper.toDoctor(request);
        doctor.setUser(user);
        doctor.setSpecialty(specialty);

        String code;
        do {
            code = AppUtils.generateEmployeeCode();
        }while (doctorRepository.existsByEmployeeCode(code));
        doctor.setEmployeeCode(code);

        String licenseNumber;
        do {
            licenseNumber = AppUtils.generateLicenseNumber();
        }while (doctorRepository.existsByLicenseNumber(licenseNumber));
        doctor.setLicenseNumber(licenseNumber);

        Doctor saved = doctorRepository.save(doctor);
        return doctorMapper.toDoctorResponse(saved);
    }

    @Transactional
    public DoctorResponse update(DoctorUpdateRequest request, Long doctorId){
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        doctorMapper.updateDoctor(request, doctor);

        doctorRepository.save(doctor);
        return doctorMapper.toDoctorResponse(doctor);
    }

    public PageResponse<DoctorResponse> findAllDoctors(Long specialtyId, String keyword, int page, int size){
        Sort sort = Sort.by(Sort.Direction.DESC, "doctorId");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Doctor> pageDoctors;

        if (specialtyId != null) {
            pageDoctors = doctorRepository.findBySpecialtyIdWithDetails(specialtyId, pageable);

        }else  {
            pageDoctors = doctorRepository.findAllDoctorsWithDetails(keyword, pageable);
        }
        return doctorMapper.toDoctorResponsePage(pageDoctors);
    }

    public DoctorResponse getDoctorById(Long doctorId){
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return doctorMapper.toDoctorResponse(doctor);
    }

    public DoctorResponse getDoctorByUserId(Long userId) {
        Doctor doctor = doctorRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return doctorMapper.toDoctorResponse(doctor);
    }

    @Transactional
    public void delete(Long doctorId){
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        userService.delete(doctor.getUser().getUserId());
    }
}
