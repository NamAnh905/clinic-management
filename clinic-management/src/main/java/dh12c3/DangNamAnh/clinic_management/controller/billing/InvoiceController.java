package dh12c3.DangNamAnh.clinic_management.controller.billing;

import dh12c3.DangNamAnh.clinic_management.dto.request.billing.InvoiceCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.billing.InvoiceUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.ApiResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.billing.InvoiceResponse;
import dh12c3.DangNamAnh.clinic_management.enums.PaymentMethod;
import dh12c3.DangNamAnh.clinic_management.enums.PaymentStatus;
import dh12c3.DangNamAnh.clinic_management.service.billing.InvoiceService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/invoices")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InvoiceController {

    InvoiceService invoiceService;

    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('CREATE_INVOICE')")
    @PostMapping
    public ApiResponse<InvoiceResponse> create(@RequestBody @Valid InvoiceCreationRequest request) {
        return ApiResponse.<InvoiceResponse>builder()
                .result(invoiceService.create(request))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('UPDATE_INVOICE')")
    @PutMapping("/{invoiceId}")
    public ApiResponse<InvoiceResponse> update(@PathVariable Long invoiceId,
                                               @RequestBody InvoiceUpdateRequest request) {
        return ApiResponse.<InvoiceResponse>builder()
                .result(invoiceService.update(invoiceId, request))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('READ_INVOICE')")
    @GetMapping
    public ApiResponse<PageResponse<InvoiceResponse>> findAll(@RequestParam(required = false)PaymentStatus paymentStatus,
                                                              @RequestParam(required = false) PaymentMethod paymentMethod,
                                                              @RequestParam(required = false) @DateTimeFormat(iso =
                                                                      DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                                              @RequestParam(required = false) @DateTimeFormat(iso =
                                                                      DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                                                              @RequestParam(required = false) String keyword,
                                                              @RequestParam(defaultValue = "1") int page,
                                                              @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<InvoiceResponse>>builder()
                .result(invoiceService.findAll(paymentStatus, paymentMethod, startDate, endDate, keyword, page, size))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('READ_INVOICE')")
    @GetMapping("/{invoiceId:\\d+}")
    public ApiResponse<InvoiceResponse> findById(@PathVariable Long invoiceId) {
        return ApiResponse.<InvoiceResponse>builder()
                .result(invoiceService.findById(invoiceId))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('READ_INVOICE') or hasAuthority('READ_OWN_INVOICE')")
    @GetMapping("/by-appointment/{appointmentId}")
    public ApiResponse<InvoiceResponse> getInvoiceByAppointment(@PathVariable Long appointmentId) {
        return ApiResponse.<InvoiceResponse>builder()
                .result(invoiceService.findByAppointmentId(appointmentId))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS')")
    @DeleteMapping("/{invoiceId}")
    public void delete(@PathVariable Long invoiceId) {
        invoiceService.delete(invoiceId);
    }
}
