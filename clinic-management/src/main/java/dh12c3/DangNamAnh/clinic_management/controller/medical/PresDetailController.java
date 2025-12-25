package dh12c3.DangNamAnh.clinic_management.controller.medical;

import dh12c3.DangNamAnh.clinic_management.dto.request.medical.PresDetailCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.medical.PresDetailUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.ApiResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.medical.PresDetailResponse;
import dh12c3.DangNamAnh.clinic_management.service.medical.PresDetailService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Builder
@RequestMapping("/pres-detail")

public class PresDetailController {
    PresDetailService presDetailService;

    @PreAuthorize("hasAuthority('CREATE_PRESCRIPTION') or hasAuthority('FULL_ACCESS')")
    @PostMapping
    public ApiResponse<PresDetailResponse> create(@RequestBody @Valid PresDetailCreationRequest request){
        return ApiResponse.<PresDetailResponse>builder()
                .result(presDetailService.create(request))
                .build();
    }

    @PreAuthorize("hasAuthority('UPDATE_PRESCRIPTION') or hasAuthority('FULL_ACCESS')")
    @PutMapping("/{detailId}")
    public ApiResponse<PresDetailResponse> update(@RequestBody PresDetailUpdateRequest request, @PathVariable Long detailId){
        return ApiResponse.<PresDetailResponse>builder()
                .result(presDetailService.update(request, detailId))
                .build();
    }

    @PreAuthorize("hasAuthority('READ_PRESCRIPTION') or hasAuthority('FULL_ACCESS')")
    @GetMapping("/by-prescription/{prescriptionId}")
    public ApiResponse<List<PresDetailResponse>> getDetailsByPrescriptionId(@PathVariable Long prescriptionId) {
        return ApiResponse.<List<PresDetailResponse>>builder()
                .result(presDetailService.findDetailsByPrescriptionId(prescriptionId))
                .build();
    }

    @PreAuthorize("hasAuthority('READ_PRESCRIPTION') or hasAuthority('FULL_ACCESS')")
    @GetMapping("/{detailId}")
    public ApiResponse<PresDetailResponse> findById(@PathVariable Long detailId){
        return ApiResponse.<PresDetailResponse>builder()
                .result(presDetailService.findById(detailId))
                .build();
    }

    @PreAuthorize("hasAuthority('DELETE_PRESCRIPTION') or hasAuthority('FULL_ACCESS')")
    @DeleteMapping("/{detailId}")
    public void delete(@PathVariable Long detailId){
        presDetailService.deleteById(detailId);
    }
}
