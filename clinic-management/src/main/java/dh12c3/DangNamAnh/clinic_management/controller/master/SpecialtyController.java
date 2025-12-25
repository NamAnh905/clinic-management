package dh12c3.DangNamAnh.clinic_management.controller.master;

import dh12c3.DangNamAnh.clinic_management.dto.request.master.SpecialtyCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.master.SpecialtyUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.ApiResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.master.SpecialtyResponse;
import dh12c3.DangNamAnh.clinic_management.service.master.SpecialtyService;
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
@RequestMapping("/specialties")

public class SpecialtyController {
    SpecialtyService specialtyService;

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @PostMapping
    public ApiResponse<SpecialtyResponse> create(@RequestBody @Valid SpecialtyCreationRequest request){
        return ApiResponse.<SpecialtyResponse>builder()
                .result(specialtyService.create(request))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @PutMapping("/{specialtyId}")
    public ApiResponse<SpecialtyResponse> update(@RequestBody SpecialtyUpdateRequest request, @PathVariable Long specialtyId){
        return ApiResponse.<SpecialtyResponse>builder()
                .result(specialtyService.update(request,specialtyId))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('READ_SPECIALTY')")
    @GetMapping("/{specialtyId}")
    public ApiResponse<SpecialtyResponse> findById(@PathVariable Long specialtyId){
        return ApiResponse.<SpecialtyResponse>builder()
                .result(specialtyService.findById(specialtyId))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('READ_SPECIALTY')")
    @GetMapping
    public ApiResponse<PageResponse<SpecialtyResponse>> findAll(@RequestParam(required = false) String keyword,
                                                                @RequestParam(defaultValue = "1") int page,
                                                                @RequestParam(defaultValue = "10") int size){
        return ApiResponse.<PageResponse<SpecialtyResponse>>builder()
                .result(specialtyService.findAll(keyword, page, size))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @DeleteMapping("/{specialtyId}")
    public void delete(@PathVariable Long specialtyId){
        specialtyService.delete(specialtyId);
    }
}
