package dh12c3.DangNamAnh.clinic_management.service.staff;

import dh12c3.DangNamAnh.clinic_management.dto.request.staff.ReceptionistCreateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.staff.ReceptionistUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.staff.ReceptionistResponse;
import dh12c3.DangNamAnh.clinic_management.entity.patient.Patient;
import dh12c3.DangNamAnh.clinic_management.entity.staff.Receptionist;
import dh12c3.DangNamAnh.clinic_management.entity.user.Role;
import dh12c3.DangNamAnh.clinic_management.entity.user.User;
import dh12c3.DangNamAnh.clinic_management.exception.AppException;
import dh12c3.DangNamAnh.clinic_management.exception.ErrorCode;
import dh12c3.DangNamAnh.clinic_management.helper.AppUtils;
import dh12c3.DangNamAnh.clinic_management.mapper.staff.ReceptionistMapper;
import dh12c3.DangNamAnh.clinic_management.repository.appoinment.AppointmentRepository;
import dh12c3.DangNamAnh.clinic_management.repository.patient.PatientRepository;
import dh12c3.DangNamAnh.clinic_management.repository.staff.ReceptionistRepository;
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
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
@Transactional(readOnly = true)

public class ReceptionistService {
    ReceptionistRepository receptionistRepository;
    ReceptionistMapper receptionistMapper;
    UserRepository userRepository;
    RoleRepository roleRepository;
    PatientRepository patientRepository;
    AppointmentRepository appointmentRepository;
    UserService userService;

    @Transactional
    public ReceptionistResponse create(ReceptionistCreateRequest request){
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        Role receceptionistRole = roleRepository.findById("RECEPTIONIST")
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

        user.setRoles(new HashSet<>(Set.of(receceptionistRole)));
        userRepository.save(user);

        Receptionist receptionist = receptionistMapper.toReceptionist(request);
        receptionist.setUser(user);

        String code;
        do {
            code = AppUtils.generateEmployeeCode();
        }while (receptionistRepository.existsByEmployeeCode(code));
        receptionist.setEmployeeCode(code);

        receptionistRepository.save(receptionist);
        return receptionistMapper.toReceptionistResponse(receptionist);
    }

    @Transactional
    public ReceptionistResponse update(ReceptionistUpdateRequest request, Long receptionistId){
        Receptionist receptionist = receptionistRepository.findById(receptionistId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        receptionistMapper.update(request,receptionist);

        receptionistRepository.save(receptionist);
        return receptionistMapper.toReceptionistResponse(receptionist);
    }

    public PageResponse<ReceptionistResponse> findAll(String keyword, int page, int size){
        Sort sort = Sort.by(Sort.Direction.ASC, "employeeCode");
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<Receptionist> receptionists = receptionistRepository.getAllReceptionistsWithDetails(keyword, pageable);

        return receptionistMapper.toPageResponse(receptionists);
    }

    public ReceptionistResponse findById(Long receptionistId){
        Receptionist receptionist = receptionistRepository.findById(receptionistId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return receptionistMapper.toReceptionistResponse(receptionist);
    }

    public ReceptionistResponse getReceptionistByUserId(Long userId){
        Receptionist receptionist = receptionistRepository.findByUser_UserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return receptionistMapper.toReceptionistResponse(receptionist);
    }

    @Transactional
    public void  deleteById(Long receptionistId){
        Receptionist receptionist = receptionistRepository.findById(receptionistId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        userService.delete(receptionist.getUser().getUserId());
    }
}
