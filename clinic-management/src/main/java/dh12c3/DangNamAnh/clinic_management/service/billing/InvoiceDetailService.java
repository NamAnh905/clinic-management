package dh12c3.DangNamAnh.clinic_management.service.billing;

import dh12c3.DangNamAnh.clinic_management.dto.request.billing.InvoiceDetailCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.billing.InvoiceDetailUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.billing.InvoiceDetailResponse;
import dh12c3.DangNamAnh.clinic_management.entity.billing.Invoice;
import dh12c3.DangNamAnh.clinic_management.entity.billing.InvoiceDetail;
import dh12c3.DangNamAnh.clinic_management.entity.master.Drug;
import dh12c3.DangNamAnh.clinic_management.entity.master.ServiceEntity;
import dh12c3.DangNamAnh.clinic_management.enums.PaymentStatus;
import dh12c3.DangNamAnh.clinic_management.exception.AppException;
import dh12c3.DangNamAnh.clinic_management.exception.ErrorCode;
import dh12c3.DangNamAnh.clinic_management.mapper.billing.InvoiceDetailMapper;
import dh12c3.DangNamAnh.clinic_management.repository.billing.InvoiceDetailRepository;
import dh12c3.DangNamAnh.clinic_management.repository.billing.InvoiceRepository;
import dh12c3.DangNamAnh.clinic_management.repository.master.DrugRepository;
import dh12c3.DangNamAnh.clinic_management.repository.master.ServiceEntityRepository;
import dh12c3.DangNamAnh.clinic_management.repository.medical.PresDetailRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)

public class InvoiceDetailService {

    InvoiceDetailRepository invoiceDetailRepository;
    InvoiceRepository invoiceRepository;
    ServiceEntityRepository serviceEntityRepository;
    DrugRepository drugRepository;
    InvoiceDetailMapper invoiceDetailMapper;
    PresDetailRepository presDetailRepository;

    @Transactional
    public InvoiceDetailResponse create(InvoiceDetailCreationRequest request) {
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        InvoiceDetail detail = invoiceDetailMapper.toInvoiceDetail(request);
        detail.setInvoice(invoice);

        if (request.getServiceId() != null) {
            ServiceEntity service = serviceEntityRepository.findById(request.getServiceId())
                    .orElseThrow(() -> new AppException(ErrorCode.SERVICE_NOT_FOUND));
            detail.setService(service);
            detail.setUnitPrice(service.getPrice());
            detail.setQuantity(request.getQuantity());
        }
        else if (request.getDrugId() != null) {
            Drug drug = drugRepository.findById(request.getDrugId())
                    .orElseThrow(() -> new AppException(ErrorCode.DRUG_NOT_FOUND));
            if (drug.getStockQuantity() < request.getQuantity()) {
                throw new AppException(ErrorCode.DRUG_OOS);
            }
            if (invoice.getPaymentStatus() == PaymentStatus.PAID) {
                drug.setStockQuantity(drug.getStockQuantity() - request.getQuantity());
                drugRepository.save(drug);
            }
            detail.setDrug(drug);
            detail.setUnitPrice(drug.getPrice());
            detail.setQuantity(request.getQuantity());
        }
        else {
            throw new AppException(ErrorCode.DATA_INVALID);
        }

        InvoiceDetail saved = invoiceDetailRepository.save(detail);
        updateInvoiceTotalAmount(invoice.getInvoiceId());
        return invoiceDetailMapper.toInvoiceDetailResponse(saved);
    }

    @Transactional
    public InvoiceDetailResponse update(Long detailId, InvoiceDetailUpdateRequest request) {
        InvoiceDetail detail = invoiceDetailRepository.findById(detailId)
                .orElseThrow(() -> new AppException(ErrorCode.DETAIL_NOT_FOUND));

        if (detail.getDrug() != null && request.getQuantity() != null) {
            Drug drug = detail.getDrug();
            int oldQty = detail.getQuantity();
            int newQty = request.getQuantity();
            int diff = newQty - oldQty;

            if (drug.getStockQuantity() < diff) {
                throw new AppException(ErrorCode.DRUG_OOS);
            }

            drug.setStockQuantity(drug.getStockQuantity() - diff);
            drugRepository.save(drug);
        }

        invoiceDetailMapper.update(request, detail);

        InvoiceDetail saved = invoiceDetailRepository.save(detail);
        return invoiceDetailMapper.toInvoiceDetailResponse(saved);
    }

    public List<InvoiceDetailResponse> findDetailsByInvoiceId(Long invoiceId) {
        return invoiceDetailRepository.findByInvoice_InvoiceId(invoiceId)
                .stream()
                .map(invoiceDetailMapper::toInvoiceDetailResponse)
                .toList();
    }

    public InvoiceDetailResponse findById(Long detailId) {
        InvoiceDetail detail = invoiceDetailRepository.findById(detailId)
                .orElseThrow(() -> new AppException(ErrorCode.DETAIL_NOT_FOUND));

        return invoiceDetailMapper.toInvoiceDetailResponse(detail);
    }

    @Transactional
    public void delete(Long detailId) {
        InvoiceDetail detail = invoiceDetailRepository.findById(detailId)
                .orElseThrow(() -> new AppException(ErrorCode.DETAIL_NOT_FOUND));

        Long invoiceId = detail.getInvoice().getInvoiceId();
        Invoice invoice = detail.getInvoice();

        if (detail.getDrug() != null && invoice.getPaymentStatus() == PaymentStatus.PAID) {
            Drug drug = detail.getDrug();
            drug.setStockQuantity(drug.getStockQuantity() + detail.getQuantity());
            drugRepository.save(drug);
        }

        invoiceDetailRepository.deleteById(detailId);
        updateInvoiceTotalAmount(invoiceId);
    }

    private void checkDrugInPrescription(Long appointmentId, Long drugId) {
        boolean isValid = presDetailRepository.isDrugInPrescription(appointmentId, drugId);

        if (!isValid) {
            throw new AppException(ErrorCode.DRUG_NOT_IN_PRESCRIPTION);
        }
    }

    private void updateInvoiceTotalAmount(Long invoiceId) {
        BigDecimal newTotal = invoiceDetailRepository.sumTotalByInvoiceId(invoiceId);
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));
        invoice.setTotalAmount(newTotal);
        invoiceRepository.save(invoice);
    }
}
