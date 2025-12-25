package dh12c3.DangNamAnh.clinic_management.controller.medical;

import dh12c3.DangNamAnh.clinic_management.dto.request.medical.MedicalRecordCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.medical.MedicalRecordUpdationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.ApiResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.medical.MedicalRecordResponse;
import dh12c3.DangNamAnh.clinic_management.service.medical.MedicalRecordService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/records")

public class MedicalRecordController {
    MedicalRecordService medicalRecordService;

    @PreAuthorize("hasAuthority('CREATE_MEDICAL_RECORD') or hasAuthority('FULL_ACCESS')")
    @PostMapping
    public ApiResponse<MedicalRecordResponse> create(@RequestBody @Valid MedicalRecordCreationRequest request){
        return ApiResponse.<MedicalRecordResponse>builder()
                .result(medicalRecordService.create(request))
                .build();
    }

    @PreAuthorize("hasAuthority('UPDATE_MEDICAL_RECORD') or hasAuthority('FULL_ACCESS')")
    @PutMapping("/{recordId}")
    public ApiResponse<MedicalRecordResponse> update(@RequestBody MedicalRecordUpdationRequest request, @PathVariable Long recordId) {
        return ApiResponse.<MedicalRecordResponse>builder()
                .result(medicalRecordService.update(request, recordId))
                .build();
    }

    @PreAuthorize("hasAuthority('READ_MEDICAL_RECORD') or hasAuthority('FULL_ACCESS') or hasAuthority('READ_OWN_MEDICAL_RECORD')")
    @GetMapping
    public ApiResponse<PageResponse<MedicalRecordResponse>> findAll(@RequestParam(required = false) Long doctorId,
                                                                    @RequestParam(required = false) @DateTimeFormat(iso =
                                                                                DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                                                    @RequestParam(required = false) @DateTimeFormat(iso =
                                                                            DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                                                                    @RequestParam(required = false) String keyword,
                                                                    @RequestParam(defaultValue = "1") int page,
                                                                    @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<MedicalRecordResponse>>builder()
                .result(medicalRecordService.findAll(doctorId, startDate, endDate, keyword, page, size))
                .build();
    }

    @PreAuthorize("hasAuthority('READ_MEDICAL_RECORD') or hasAuthority('FULL_ACCESS')")
    @GetMapping("/{recordId}")
    public ApiResponse<MedicalRecordResponse> findById(@PathVariable Long recordId) {
        return ApiResponse.<MedicalRecordResponse>builder()
                .result(medicalRecordService.findById(recordId))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @DeleteMapping("/{recordId}")
    public void delete(@PathVariable Long recordId) {
        medicalRecordService.delete(recordId);
    }
}
