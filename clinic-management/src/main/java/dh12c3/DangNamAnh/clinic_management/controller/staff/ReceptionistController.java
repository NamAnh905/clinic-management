package dh12c3.DangNamAnh.clinic_management.controller.staff;

import dh12c3.DangNamAnh.clinic_management.dto.request.staff.ReceptionistCreateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.staff.ReceptionistUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.ApiResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.staff.DoctorResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.staff.ReceptionistResponse;
import dh12c3.DangNamAnh.clinic_management.service.staff.ReceptionistService;
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
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE,  makeFinal = true)
@RequestMapping("/receptionists")

public class ReceptionistController {
    ReceptionistService receptionistService;

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @PostMapping
    public ApiResponse<ReceptionistResponse> create(@RequestBody @Valid ReceptionistCreateRequest request){
        return ApiResponse.<ReceptionistResponse>builder()
                .result(receptionistService.create(request))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @PutMapping("/{receptionistId}")
    public ApiResponse<ReceptionistResponse> update(@RequestBody ReceptionistUpdateRequest request, @PathVariable Long receptionistId){
        return ApiResponse.<ReceptionistResponse>builder()
                .result(receptionistService.update(request, receptionistId))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('READ_RECEPTIONIST')")
    @GetMapping
    public ApiResponse<PageResponse<ReceptionistResponse>> findAll(@RequestParam(required = false) String keyword,
                                                                   @RequestParam(defaultValue = "1") int page,
                                                                   @RequestParam(defaultValue = "10") int size){
        return ApiResponse.<PageResponse<ReceptionistResponse>>builder()
                .result(receptionistService.findAll(keyword, page, size))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @GetMapping("/{receptionistId}")
    public ApiResponse<ReceptionistResponse> findById(@PathVariable Long receptionistId){
        return ApiResponse.<ReceptionistResponse>builder()
                .result(receptionistService.findById(receptionistId))
                .build();
    }

    @GetMapping("/find-by-user/{userId}")
     @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('READ_DOCTOR') or hasAuthority('READ_OWN_APPOINTMENT')")
    public ApiResponse<ReceptionistResponse> getReceptionistByUserId(@PathVariable Long userId) {
        return ApiResponse.<ReceptionistResponse>builder()
                .result(receptionistService.getReceptionistByUserId(userId))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @DeleteMapping("/{receptionistId}")
    public void deleteById(@PathVariable Long receptionistId){
        receptionistService.deleteById(receptionistId);
    }
}
