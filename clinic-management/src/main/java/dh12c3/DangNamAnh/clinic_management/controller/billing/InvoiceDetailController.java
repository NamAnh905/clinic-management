package dh12c3.DangNamAnh.clinic_management.controller.billing;

import dh12c3.DangNamAnh.clinic_management.dto.request.billing.InvoiceDetailCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.billing.InvoiceDetailUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.ApiResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.billing.InvoiceDetailResponse;
import dh12c3.DangNamAnh.clinic_management.service.billing.InvoiceDetailService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/invoice-details")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InvoiceDetailController {

    InvoiceDetailService invoiceDetailService;

    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('CREATE_INVOICE')")
    @PostMapping
    public ApiResponse<InvoiceDetailResponse> create(@RequestBody @Valid InvoiceDetailCreationRequest request) {
        return ApiResponse.<InvoiceDetailResponse>builder()
                .result(invoiceDetailService.create(request))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('UPDATE_INVOICE')")
    @PutMapping("/{detailId}")
    public ApiResponse<InvoiceDetailResponse> update(@PathVariable Long detailId,
                                                     @RequestBody InvoiceDetailUpdateRequest request) {
        return ApiResponse.<InvoiceDetailResponse>builder()
                .result(invoiceDetailService.update(detailId, request))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('READ_INVOICE')")
    @GetMapping("/by-invoice/{invoiceId}")
    public ApiResponse<List<InvoiceDetailResponse>> findDetailsByInvoiceId(@PathVariable Long invoiceId) {
        return ApiResponse.<List<InvoiceDetailResponse>>builder()
                .result(invoiceDetailService.findDetailsByInvoiceId(invoiceId))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('READ_INVOICE')")
    @GetMapping("/{detailId}")
    public ApiResponse<InvoiceDetailResponse> findById(@PathVariable Long detailId) {
        return ApiResponse.<InvoiceDetailResponse>builder()
                .result(invoiceDetailService.findById(detailId))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @DeleteMapping("/{detailId}")
    public void delete(@PathVariable Long detailId) {
        invoiceDetailService.delete(detailId);
    }
}
