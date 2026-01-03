package dh12c3.DangNamAnh.clinic_management.controller;

import dh12c3.DangNamAnh.clinic_management.dto.response.ApiResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.billing.InvoiceResponse;
import dh12c3.DangNamAnh.clinic_management.service.billing.InvoiceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

public class PaymentController {

    InvoiceService invoiceService;

    @GetMapping("/vnpay/{invoiceId}")
    public ApiResponse<String> createVnPayUrl(@PathVariable Long invoiceId, HttpServletRequest request) {
        return ApiResponse.<String>builder()
                .result(invoiceService.createVnPayUrl(invoiceId, request))
                .build();
    }

    @GetMapping("/vnpay-return")
    public ApiResponse<InvoiceResponse> vnPayReturn(@RequestParam Map<String, String> queryParams) {
        return ApiResponse.<InvoiceResponse>builder()
                .result(invoiceService.processVnPayCallback(queryParams))
                .build();
    }
}