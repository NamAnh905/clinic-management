package dh12c3.DangNamAnh.clinic_management.controller.master;

import dh12c3.DangNamAnh.clinic_management.dto.request.master.SECreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.master.SEUpdationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.ApiResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.master.ServiceEntityResponse;
import dh12c3.DangNamAnh.clinic_management.enums.ServiceType;
import dh12c3.DangNamAnh.clinic_management.service.master.SEService;
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
@RequestMapping("/services")

public class ServiceEntityController {
    SEService service;

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @PostMapping
    public ApiResponse<ServiceEntityResponse> create(@RequestBody @Valid SECreationRequest request){
        return ApiResponse.<ServiceEntityResponse>builder()
                .result(service.create(request))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @PutMapping("/{serviceId}")
    public ApiResponse<ServiceEntityResponse> update(@PathVariable Long serviceId, @RequestBody SEUpdationRequest request){
        return ApiResponse.<ServiceEntityResponse>builder()
                .result(service.update(request, serviceId))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('READ_SERVICE')")
    @GetMapping
    public ApiResponse<PageResponse<ServiceEntityResponse>> findAll(@RequestParam(required = false) ServiceType type,
                                                                    @RequestParam(required = false) String keyword,
                                                                    @RequestParam(defaultValue = "1") int page,
                                                                    @RequestParam(defaultValue = "10") int size){
        return ApiResponse.<PageResponse<ServiceEntityResponse>>builder()
                .result(service.findAll(type, keyword, page, size))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('READ_SERVICE')")
    @GetMapping("/{serviceId}")
    public ApiResponse<ServiceEntityResponse> findById(@PathVariable Long serviceId){
        return ApiResponse.<ServiceEntityResponse>builder()
                .result(service.findById(serviceId))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @DeleteMapping("/{serviceId}")
    public void delete(@PathVariable Long serviceId){
        service.delete(serviceId);
    }
}
