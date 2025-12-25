package dh12c3.DangNamAnh.clinic_management.controller.staff;

import dh12c3.DangNamAnh.clinic_management.dto.request.staff.DoctorCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.staff.DoctorUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.ApiResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.staff.DoctorResponse;
import dh12c3.DangNamAnh.clinic_management.service.staff.DoctorService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/doctors")

public class DoctorController {
    DoctorService doctorService;

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @PostMapping
    public ApiResponse<DoctorResponse> create(@RequestBody @Valid DoctorCreationRequest request){
        return ApiResponse.<DoctorResponse>builder()
                .result(doctorService.create(request))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @PutMapping("/{doctorId}")
    public ApiResponse<DoctorResponse> update(@RequestBody DoctorUpdateRequest request, @PathVariable Long doctorId){
        return ApiResponse.<DoctorResponse>builder()
                .result(doctorService.update(request,doctorId))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('READ_DOCTOR')")
    @GetMapping("/{doctorId}")
    public ApiResponse<DoctorResponse> getDoctorById(@PathVariable Long doctorId){
        return ApiResponse.<DoctorResponse>builder()
                .result(doctorService.getDoctorById(doctorId))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('READ_DOCTOR')")
    @GetMapping
    public ApiResponse<PageResponse<DoctorResponse>> getAllDoctors(@RequestParam(required = false) String keyword,
                                                                       @RequestParam(required = false) Long specialtyId,
                                                                       @RequestParam(defaultValue = "1") int page,
                                                                       @RequestParam(defaultValue = "10") int size){
        return ApiResponse.<PageResponse<DoctorResponse>>builder()
                .result(doctorService.findAllDoctors(specialtyId, keyword, page, size))
                .build();
    }

    @GetMapping("/find-by-user/{userId}")
    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('READ_DOCTOR') or hasAuthority('READ_OWN_APPOINTMENT')")
    public ApiResponse<DoctorResponse> getDoctorByUserId(@PathVariable Long userId) {
        return ApiResponse.<DoctorResponse>builder()
                .result(doctorService.getDoctorByUserId(userId))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @DeleteMapping("/{doctorId}")
    public void delete(@PathVariable Long doctorId){
        doctorService.delete(doctorId);
    }
}
