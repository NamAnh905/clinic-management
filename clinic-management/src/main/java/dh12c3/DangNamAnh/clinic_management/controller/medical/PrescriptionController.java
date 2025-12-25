package dh12c3.DangNamAnh.clinic_management.controller.medical;

import dh12c3.DangNamAnh.clinic_management.dto.request.medical.PrescriptionCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.medical.PrescriptionUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.ApiResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.medical.PrescriptionResponse;
import dh12c3.DangNamAnh.clinic_management.service.medical.PrescriptionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Builder
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/prescriptions")
public class PrescriptionController {

    PrescriptionService prescriptionService;

    @PreAuthorize("hasAuthority('CREATE_PRESCRIPTION') or hasAuthority('FULL_ACCESS')")
    @PostMapping
    public ApiResponse<PrescriptionResponse> create(@RequestBody @Valid PrescriptionCreationRequest request) {
        return ApiResponse.<PrescriptionResponse>builder()
                .result(prescriptionService.create(request))
                .build();
    }

    @PreAuthorize("hasAuthority('UPDATE_PRESCRIPTION') or hasAuthority('FULL_ACCESS')")
    @PutMapping("/{prescriptionId}")
    public ApiResponse<PrescriptionResponse> update(@RequestBody PrescriptionUpdateRequest request, @PathVariable Long prescriptionId) {
        return ApiResponse.<PrescriptionResponse>builder()
                .result(prescriptionService.update(request, prescriptionId))
                .build();
    }

    @PreAuthorize("hasAuthority('READ_PRESCRIPTION') or hasAuthority('FULL_ACCESS') or hasAuthority('READ_OWN_PRESCRIPTION')")
    @GetMapping
    public ApiResponse<PageResponse<PrescriptionResponse>> findAll(@RequestParam(defaultValue = "1") int page,
                                                                   @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<PrescriptionResponse>>builder()
                .result(prescriptionService.findAll(page, size))
                .build();
    }

    @PreAuthorize("hasAuthority('READ_PRESCRIPTION') or hasAuthority('FULL_ACCESS')")
    @GetMapping("/{prescriptionId}")
    public ApiResponse<PrescriptionResponse> findById(@PathVariable Long prescriptionId) {
        return ApiResponse.<PrescriptionResponse>builder()
                .result(prescriptionService.findById(prescriptionId))
                .build();
    }

    @PreAuthorize("hasAuthority('READ_PRESCRIPTION') or hasAuthority('FULL_ACCESS')")
    @GetMapping("/record/{recordId}")
    public ApiResponse<PrescriptionResponse> getByRecordId(@PathVariable Long recordId) {
        return ApiResponse.<PrescriptionResponse>builder()
                .result(prescriptionService.findByRecordId(recordId))
                .build();
    }

    @PreAuthorize("hasAuthority('DELETE_PRESCRIPTION') or hasAuthority('FULL_ACCESS')")
    @DeleteMapping("/{prescriptionId}")
    public void delete(@PathVariable Long prescriptionId) {
        prescriptionService.delete(prescriptionId);
    }
}
