package dh12c3.DangNamAnh.clinic_management.mapper.billing;

import dh12c3.DangNamAnh.clinic_management.dto.request.billing.InvoiceDetailCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.billing.InvoiceDetailUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.billing.InvoiceDetailResponse;
import dh12c3.DangNamAnh.clinic_management.entity.billing.Invoice;
import dh12c3.DangNamAnh.clinic_management.entity.billing.InvoiceDetail;
import dh12c3.DangNamAnh.clinic_management.entity.master.ServiceEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {Invoice.class, ServiceEntity.class})
public interface InvoiceDetailMapper {

    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "drug",  ignore = true)
    InvoiceDetail toInvoiceDetail(InvoiceDetailCreationRequest request);

    @Mapping(target = "detailId", source = "invoiceDetailId")
    @Mapping(target = "serviceId", source = "service.serviceId")
    @Mapping(target = "serviceName", source = "service.name")
    @Mapping(target = "drugId", source = "drug.drugId")
    @Mapping(target = "drugName", source = "drug.name")
    InvoiceDetailResponse toInvoiceDetailResponse(InvoiceDetail invoiceDetail);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "invoice", ignore = true)
    @Mapping(target = "service", ignore = true)
    @Mapping(target = "drug",  ignore = true)
    void update(InvoiceDetailUpdateRequest request, @MappingTarget InvoiceDetail invoiceDetail);
}
