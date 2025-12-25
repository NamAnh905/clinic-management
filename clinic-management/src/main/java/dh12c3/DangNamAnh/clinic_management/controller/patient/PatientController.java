package dh12c3.DangNamAnh.clinic_management.controller.patient;

import dh12c3.DangNamAnh.clinic_management.dto.request.patient.PatientCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.patient.PatientUpdationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.ApiResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.patient.PatientResponse;
import dh12c3.DangNamAnh.clinic_management.service.patient.PatientService;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/patients")

public class PatientController {
    PatientService patientService;

//    @PreAuthorize("hasAuthority('FULL_ACCESS')")
//    @PostMapping
//    public ApiResponse<PatientResponse> create(@RequestBody PatientCreationRequest request) {
//        return ApiResponse.<PatientResponse>builder()
//                .result(patientService.create(request))
//                .build();
//    }

    @PreAuthorize("hasAuthority('READ_ALL_PATIENT') or hasAuthority('FULL_ACCESS')")
    @GetMapping
    public ApiResponse<PageResponse<PatientResponse>> findAll(@RequestParam(required = false) String keyword,
                                                              @RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<PatientResponse>>builder()
                .result(patientService.findAll(keyword, page, size))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @PutMapping("/{patientId}")
    public ApiResponse<PatientResponse> update(@RequestBody PatientUpdationRequest request, @PathVariable Long patientId) {
        return ApiResponse.<PatientResponse>builder()
                .result(patientService.update(request, patientId))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('READ_ALL_PATIENT')")
    @GetMapping("/{patientId}")
    public ApiResponse<PatientResponse> findById(@PathVariable Long patientId) {
        return ApiResponse.<PatientResponse>builder()
                .result(patientService.findById(patientId))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @DeleteMapping("/{patientId}")
    public void delete(@PathVariable Long patientId) {
        patientService.delete(patientId);
    }
}
