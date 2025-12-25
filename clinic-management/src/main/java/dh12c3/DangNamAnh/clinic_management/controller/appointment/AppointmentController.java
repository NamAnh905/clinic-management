package dh12c3.DangNamAnh.clinic_management.controller.appointment;

import dh12c3.DangNamAnh.clinic_management.dto.request.appointment.AppointmentCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.appointment.AppointmentUpdationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.ApiResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.appointment.AppointmentResponse;
import dh12c3.DangNamAnh.clinic_management.enums.AppointmentStatus;
import dh12c3.DangNamAnh.clinic_management.service.appointment.AppointmentService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@Builder
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/appointments")

public class AppointmentController {
    AppointmentService appointmentService;

    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('CREATE_APPOINTMENT')")
    @PostMapping
    public ApiResponse<AppointmentResponse> create(@RequestBody @Valid AppointmentCreationRequest request){
        return ApiResponse.<AppointmentResponse>builder()
                .result(appointmentService.create(request))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('UPDATE_APPOINTMENT')")
    @PutMapping("/{appointmentId}")
    public ApiResponse<AppointmentResponse> update(@RequestBody AppointmentUpdationRequest request, @PathVariable long appointmentId){
        return ApiResponse.<AppointmentResponse>builder()
                .result(appointmentService.update(request, appointmentId))
                .build();
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('READ_APPOINTMENT') or hasAuthority('READ_OWN_APPOINTMENT')")
    @GetMapping
    public ApiResponse<PageResponse<AppointmentResponse>> findAll(@RequestParam(required = false ) Long doctorId,
                                                          @RequestParam(required = false ) Long patientId,
                                                          @RequestParam(required = false ) AppointmentStatus status,
                                                          @RequestParam(required = false) @DateTimeFormat(iso =
                                                                  DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                                          @RequestParam(required = false) @DateTimeFormat(iso =
                                                                  DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
                                                          @RequestParam(required = false) String keyword,
                                                          @RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "10") int size
    ){
        return ApiResponse.<PageResponse<AppointmentResponse>>builder()
                .result(appointmentService.findAll(doctorId, patientId, status, startDate, endDate, keyword, page, size))
                .build();
    }

    @GetMapping("/{appointmentId}")
    public ApiResponse<AppointmentResponse> findById(@PathVariable Long appointmentId){
        return ApiResponse.<AppointmentResponse>builder()
                .result(appointmentService.findById(appointmentId))
                .build();
    }

    @DeleteMapping("/{appointmentId}")
    public void delete(@PathVariable Long appointmentId){
        appointmentService.delete(appointmentId);
    }

    @PreAuthorize("hasAuthority('FULL_ACCESS') or hasAuthority('READ_APPOINTMENT')")
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportAppointments() throws IOException {
        ByteArrayInputStream in = appointmentService.exportAppointments();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=appointments_full.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
}
