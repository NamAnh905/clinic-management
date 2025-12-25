package dh12c3.DangNamAnh.clinic_management.mapper.billing;

import dh12c3.DangNamAnh.clinic_management.dto.request.billing.InvoiceCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.billing.InvoiceUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.billing.InvoiceResponse;
import dh12c3.DangNamAnh.clinic_management.entity.billing.Invoice;
import org.mapstruct.*;
import org.springframework.data.domain.Page;

@Mapper(componentModel = "spring")
public interface InvoiceMapper {

    @Mapping(target = "invoiceId", ignore = true)
    @Mapping(target = "appointment", ignore = true)
    @Mapping(target = "invoiceDetails", ignore = true) // để sau
    Invoice toInvoice(InvoiceCreationRequest request);

    @Mapping(target = "appointmentId", source = "appointment.appointmentId")
    @Mapping(target = "patientName", source = "appointment.patient.user.fullName")
    @Mapping(target = "doctorName", source = "appointment.doctor.user.fullName")
    InvoiceResponse toInvoiceResponse(Invoice invoice);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "appointment", ignore = true)
    @Mapping(target = "invoiceDetails", ignore = true)
    void update(InvoiceUpdateRequest request, @MappingTarget Invoice invoice);

    default PageResponse<InvoiceResponse> toInvoicePage(Page<Invoice> page) {
        if (page == null) {
            return null;
        }

        return PageResponse.<InvoiceResponse>builder()
                .currentPage(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .data(page.getContent().stream()
                        .map(this::toInvoiceResponse)
                        .toList())
                .build();
    }
}
