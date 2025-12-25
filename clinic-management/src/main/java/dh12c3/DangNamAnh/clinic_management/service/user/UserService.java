package dh12c3.DangNamAnh.clinic_management.service.user;

import dh12c3.DangNamAnh.clinic_management.dto.request.user.UserCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.user.UserUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.user.UserResponse;
import dh12c3.DangNamAnh.clinic_management.entity.patient.Patient;
import dh12c3.DangNamAnh.clinic_management.entity.user.Role;
import dh12c3.DangNamAnh.clinic_management.entity.user.User;
import dh12c3.DangNamAnh.clinic_management.exception.AppException;
import dh12c3.DangNamAnh.clinic_management.exception.ErrorCode;
import dh12c3.DangNamAnh.clinic_management.mapper.user.UserMapper;
import dh12c3.DangNamAnh.clinic_management.repository.appoinment.AppointmentRepository;
import dh12c3.DangNamAnh.clinic_management.repository.patient.PatientRepository;
import dh12c3.DangNamAnh.clinic_management.repository.user.RoleRepository;
import dh12c3.DangNamAnh.clinic_management.repository.user.UserRepository;
import dh12c3.DangNamAnh.clinic_management.service.ExcelExportService;
import dh12c3.DangNamAnh.clinic_management.repository.user.UserSpecs;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)

public class UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    PatientRepository patientRepository;
    AppointmentRepository appointmentRepository;
    ExcelExportService excelExportService;

    @Transactional
    public UserResponse register(UserCreationRequest request){
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EXISTED_EMAIL);
        }

        User user = userMapper.toUser(request);

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setActive(true);

        var roles = new HashSet<Role>();
        roleRepository.findById("PATIENT").ifPresent(roles::add);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        Patient patient = new Patient();
        patient.setUser(savedUser);
        patientRepository.save(patient);

        return userMapper.toUserResponse(savedUser);
    }

    public UserResponse getUserById(Long userId){
        return userMapper.toUserResponse(userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND)));
    }

    public PageResponse<UserResponse> getAllUsers(Boolean status, String roleName, String keyword, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.ASC, "userId");
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Specification<User> spec = (root, q, cb) -> cb.conjunction();

        if (status != null) {
            spec = spec.and(UserSpecs.hasStatus(status));
        }
        if (roleName != null) {
            spec = spec.and(UserSpecs.hasRole(roleName));
        }
        if (keyword != null) {
            spec = spec.and(UserSpecs.containsKeyword(keyword));
        }

        Page<User> users = userRepository.findAll(spec, pageable);
        return userMapper.toPageResponse(users);
    }

    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        return userMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse update(UserUpdateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            Set<String> currentRoles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.toSet());
            Set<String> newRoles = new HashSet<>(request.getRoles());

            if (currentRoles.contains("DOCTOR") || currentRoles.contains("RECEPTIONIST")) {
                if (!newRoles.equals(currentRoles)) {
                    throw new AppException(ErrorCode.STAFF_ROLE_CHANGE_DENIED);
                }
            }

            if (currentRoles.contains("PATIENT") && (newRoles.contains("DOCTOR") || newRoles.contains("RECEPTIONIST"))) {
                var patientOpt = patientRepository.findByUser_UserId(userId);
                if (patientOpt.isPresent()) {
                    boolean hasHistory = appointmentRepository.existsByPatient_PatientId(patientOpt.get().getPatientId());
                    if (hasHistory) {
                        throw new AppException(ErrorCode.PATIENT_HAS_HISTORY);
                    }
                }
            }

            var roles = roleRepository.findAllById(request.getRoles());
            user.setRoles(new HashSet<>(roles));
        }

        userMapper.updateUser(request, user);
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getIsActive() != null) {
            user.setActive(request.getIsActive());
        }

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateMyInfo(UserUpdateRequest request) {
        var context = SecurityContextHolder.getContext();
        String email = context.getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            if (request.getOldPassword() == null || request.getOldPassword().isBlank()) {
                throw new RuntimeException("Vui lòng nhập mật khẩu hiện tại để thay đổi mật khẩu mới.");
            }

            if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
                throw new AppException(ErrorCode.INVALID_PASSWORD);
            }

            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        userMapper.updateUser(request, user);
        if (request.getRoles() != null) {
            log.warn("Attempt to change role during self-update by user: {}", email);
        }

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @Transactional
    public void delete(Long userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        user.setActive(false);
        userRepository.save(user);
    }

    public ByteArrayInputStream loadUsersAndExportToExcel() throws IOException, IllegalAccessException {
        List<UserResponse> users = userRepository.findAll()
                .stream()
                .map(userMapper::toUserResponse)
                .toList();

        return excelExportService.exportToExcel(users, "Danh sách người dùng");
    }
}
