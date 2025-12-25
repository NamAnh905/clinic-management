package dh12c3.DangNamAnh.clinic_management.controller.master;

import dh12c3.DangNamAnh.clinic_management.dto.request.master.DrugCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.master.DrugUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.ApiResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.master.DrugResponse;
import dh12c3.DangNamAnh.clinic_management.service.master.DrugService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/drugs")
public class DrugController {

    DrugService drugService;

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @PostMapping
    public ApiResponse<DrugResponse> create(@RequestBody @Valid DrugCreationRequest request) {
        return ApiResponse.<DrugResponse>builder()
                .result(drugService.create(request))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @PutMapping("/{drugId}")
    public ApiResponse<DrugResponse> update(@RequestBody DrugUpdateRequest request, @PathVariable Long drugId) {
        return ApiResponse.<DrugResponse>builder()
                .result(drugService.update(request, drugId))
                .build();
    }

    @PreAuthorize("hasAuthority('READ_DRUG') or hasAuthority('FULL_ACCESS')")
    @GetMapping
    public ApiResponse<PageResponse<DrugResponse>> findAll(@RequestParam(required = false) String keyword,
                                                           @RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<DrugResponse>>builder()
                .result(drugService.findAll(keyword, page, size))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('READ_DRUG')")
    @GetMapping("/{drugId}")
    public ApiResponse<DrugResponse> findById(@PathVariable Long drugId) {
        return ApiResponse.<DrugResponse>builder()
                .result(drugService.findById(drugId))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @DeleteMapping("/{drugId}")
    public void delete(@PathVariable Long drugId) {
        drugService.delete(drugId);
    }

    @PreAuthorize("hasAuthority('READ_DRUG') or hasAuthority('FULL_ACCESS')")
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportDrugs() throws IOException {
        ByteArrayInputStream in = drugService.exportDrugs();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=drugs_export.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}
