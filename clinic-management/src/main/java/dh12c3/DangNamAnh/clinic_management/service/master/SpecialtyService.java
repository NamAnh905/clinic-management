package dh12c3.DangNamAnh.clinic_management.service.master;

import dh12c3.DangNamAnh.clinic_management.dto.request.master.SpecialtyCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.master.SpecialtyUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.master.SpecialtyResponse;
import dh12c3.DangNamAnh.clinic_management.entity.master.Specialty;
import dh12c3.DangNamAnh.clinic_management.exception.AppException;
import dh12c3.DangNamAnh.clinic_management.exception.ErrorCode;
import dh12c3.DangNamAnh.clinic_management.mapper.master.SpecialtyMapper;
import dh12c3.DangNamAnh.clinic_management.repository.master.SpecialtyRepository;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
@Transactional(readOnly = true)

public class SpecialtyService {
    SpecialtyMapper specialtyMapper;
    SpecialtyRepository  specialtyRepository;
    DoctorRepository doctorRepository;

    @Transactional
    public SpecialtyResponse create(SpecialtyCreationRequest request) {
        Specialty specialty = specialtyMapper.toSpecialty(request);
        if (request.getDoctors() != null && !request.getDoctors().isEmpty()) {
            var doctors = doctorRepository.findAllById(request.getDoctors());
            specialty.setDoctors(new HashSet<>(doctors));
        } else {
            specialty.setDoctors(new HashSet<>());
        }

        specialtyRepository.save(specialty);
        return specialtyMapper.toSpecialtyResponse(specialty);
    }

    @Transactional
    public SpecialtyResponse update(SpecialtyUpdateRequest request, Long specialtyId) {
        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(()->new AppException(ErrorCode.SPECIALTY_NOT_FOUND));

        specialtyMapper.toUpdate(request,specialty);

        if (request.getDoctors() != null && !request.getDoctors().isEmpty()) {
            var doctors = doctorRepository.findAllById(request.getDoctors());
            specialty.setDoctors(new HashSet<>(doctors));
        }

        return specialtyMapper.toSpecialtyResponse(specialtyRepository.save(specialty));

    }

    public SpecialtyResponse findById(Long specialtyId) {
        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(()->new AppException(ErrorCode.SPECIALTY_NOT_FOUND));
        return specialtyMapper.toSpecialtyResponse(specialty);
    }

    public PageResponse<SpecialtyResponse> findAll(String keyword, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.ASC, "specialtyId");
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<Specialty> specialties = specialtyRepository.getAllSpecialties(keyword, pageable);

        return specialtyMapper.toSpecialtyPage(specialties);
    }

    @Transactional
    public void delete(Long specialtyId) {
        Specialty specialty = specialtyRepository.findById(specialtyId)
                .orElseThrow(()->new AppException(ErrorCode.SPECIALTY_NOT_FOUND));

        specialty.setDeleted(true);
        specialtyRepository.save(specialty);
    }
}
