package dh12c3.DangNamAnh.clinic_management.service.billing;

import dh12c3.DangNamAnh.clinic_management.component.SecurityUtils;
import dh12c3.DangNamAnh.clinic_management.dto.request.billing.InvoiceCreationRequest;
import dh12c3.DangNamAnh.clinic_management.dto.request.billing.InvoiceUpdateRequest;
import dh12c3.DangNamAnh.clinic_management.dto.response.PageResponse;
import dh12c3.DangNamAnh.clinic_management.dto.response.billing.InvoiceResponse;
import dh12c3.DangNamAnh.clinic_management.entity.appointment.Appointment;
import dh12c3.DangNamAnh.clinic_management.entity.billing.Invoice;
import dh12c3.DangNamAnh.clinic_management.entity.billing.InvoiceDetail;
import dh12c3.DangNamAnh.clinic_management.entity.master.Drug;
import dh12c3.DangNamAnh.clinic_management.entity.master.ServiceEntity;
import dh12c3.DangNamAnh.clinic_management.entity.master.Specialty;
import dh12c3.DangNamAnh.clinic_management.entity.medical.Prescription;
import dh12c3.DangNamAnh.clinic_management.entity.medical.PrescriptionDetail;
import dh12c3.DangNamAnh.clinic_management.entity.patient.Patient;
import dh12c3.DangNamAnh.clinic_management.enums.AppointmentStatus;
import dh12c3.DangNamAnh.clinic_management.enums.PaymentMethod;
import dh12c3.DangNamAnh.clinic_management.enums.PaymentStatus;
import dh12c3.DangNamAnh.clinic_management.exception.AppException;
import dh12c3.DangNamAnh.clinic_management.exception.ErrorCode;
import dh12c3.DangNamAnh.clinic_management.helper.AppUtils;
import dh12c3.DangNamAnh.clinic_management.mapper.billing.InvoiceMapper;
import dh12c3.DangNamAnh.clinic_management.repository.appoinment.AppointmentRepository;
import dh12c3.DangNamAnh.clinic_management.repository.billing.InvoiceDetailRepository;
import dh12c3.DangNamAnh.clinic_management.repository.billing.InvoiceRepository;
import dh12c3.DangNamAnh.clinic_management.repository.master.DrugRepository;
import dh12c3.DangNamAnh.clinic_management.repository.master.ServiceEntityRepository;
import dh12c3.DangNamAnh.clinic_management.repository.medical.PresDetailRepository;
import dh12c3.DangNamAnh.clinic_management.repository.medical.PrescriptionRepository;
import dh12c3.DangNamAnh.clinic_management.repository.patient.PatientRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional(readOnly = true)

public class InvoiceService {

    InvoiceRepository invoiceRepository;
    AppointmentRepository appointmentRepository;
    InvoiceMapper invoiceMapper;
    PatientRepository patientRepository;
    InvoiceDetailRepository invoiceDetailRepository;
    ServiceEntityRepository  serviceEntityRepository;
    PrescriptionRepository prescriptionRepository;
    PresDetailRepository  presDetailRepository;
    DrugRepository drugRepository;

    SecurityUtils securityUtils;

    @Transactional
    public InvoiceResponse create(InvoiceCreationRequest request) {
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new AppException(ErrorCode.CANNOT_CREATE_INVOICE);
        }

        if (invoiceRepository.existsByAppointment_AppointmentId(request.getAppointmentId())){
            throw new AppException(ErrorCode.INVOICE_ALREADY_EXISTS);
        }

        Invoice invoice = invoiceMapper.toInvoice(request);
        invoice.setAppointment(appointment);
        invoice.setTotalAmount(BigDecimal.ZERO);
        invoice.setTransactionCode(AppUtils.generateTransactionCode());
        invoice.setPaymentStatus(PaymentStatus.PENDING);

        Invoice savedInvoice = invoiceRepository.save(invoice);
        BigDecimal totalAmount = BigDecimal.ZERO;

        Specialty specialty = appointment.getDoctor().getSpecialty();
        ServiceEntity consultationService = specialty.getDefaultService();
        if (consultationService != null) {
            InvoiceDetail examDetail = new InvoiceDetail();
            examDetail.setInvoice(savedInvoice);
            examDetail.setService(consultationService);
            examDetail.setQuantity(1);
            examDetail.setUnitPrice(consultationService.getPrice());
            invoiceDetailRepository.save(examDetail);
            totalAmount = totalAmount.add(consultationService.getPrice());
        }

        if (request.getServiceIds() != null && !request.getServiceIds().isEmpty()) {
            List<ServiceEntity> services = serviceEntityRepository.findAllById(request.getServiceIds());
            for (ServiceEntity service : services) {
                if (consultationService != null && service.getServiceId().equals(consultationService.getServiceId())) continue;

                InvoiceDetail serviceDetail = new InvoiceDetail();
                serviceDetail.setInvoice(savedInvoice);
                serviceDetail.setService(service);
                serviceDetail.setQuantity(1);
                serviceDetail.setUnitPrice(service.getPrice());
                invoiceDetailRepository.save(serviceDetail);
                totalAmount = totalAmount.add(service.getPrice());
            }
        }

        Prescription prescription = prescriptionRepository.findByMedicalRecord_Appointment_AppointmentId(request.getAppointmentId()).orElse(null);
        if (prescription != null) {
            List<PrescriptionDetail> presDetails = presDetailRepository.findByPrescription_PrescriptionId(prescription.getPrescriptionId());
            for (PrescriptionDetail pd : presDetails) {
                Drug drug = pd.getDrug();

                InvoiceDetail drugDetail = new InvoiceDetail();
                drugDetail.setInvoice(savedInvoice);
                drugDetail.setDrug(drug);
                drugDetail.setQuantity(pd.getQuantity());
                drugDetail.setUnitPrice(drug.getPrice());

                invoiceDetailRepository.save(drugDetail);

                BigDecimal drugTotal = drug.getPrice().multiply(BigDecimal.valueOf(pd.getQuantity()));
                totalAmount = totalAmount.add(drugTotal);
            }
        }

        savedInvoice.setTotalAmount(totalAmount);
        return invoiceMapper.toInvoiceResponse(invoiceRepository.save(savedInvoice));
    }

    @Transactional
    public InvoiceResponse update(Long invoiceId, InvoiceUpdateRequest request) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        PaymentStatus oldStatus = invoice.getPaymentStatus();
        PaymentStatus newStatus = request.getPaymentStatus();

        if (newStatus == PaymentStatus.PAID && oldStatus != PaymentStatus.PAID) {
            Set<InvoiceDetail> details = invoice.getInvoiceDetails();
            if (details != null) {
                for (InvoiceDetail detail : details) {
                    if (detail.getDrug() != null) {
                        Drug drug = detail.getDrug();
                        int quantityToDeduct = detail.getQuantity();

                        if (drug.getStockQuantity() < quantityToDeduct) {
                            throw new AppException(ErrorCode.DRUG_OOS);
                        }

                        drug.setStockQuantity(drug.getStockQuantity() - quantityToDeduct);
                        drugRepository.save(drug);
                    }
                }
            }
        }

        invoiceMapper.update(request, invoice);
        return invoiceMapper.toInvoiceResponse(invoiceRepository.save(invoice));
    }

    public InvoiceResponse findById(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        String currentUsername = securityUtils.getCurrentUserLogin();
        boolean canSeeAll = securityUtils.hasRole("READ_INVOICE");

        if (!canSeeAll) {
            String ownerEmail = invoice.getAppointment().getPatient().getUser().getEmail();
            if (!ownerEmail.equals(currentUsername)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }
        return invoiceMapper.toInvoiceResponse(invoice);
    }

    public PageResponse<InvoiceResponse> findAll(PaymentStatus paymentStatus,
                                                 PaymentMethod paymentMethod,
                                                 LocalDateTime startDate,
                                                 LocalDateTime endDate,
                                                 String keyword,
                                                 int page, int size) {
        Sort sort = Sort.by(Sort.Direction.ASC, "invoiceId");
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        String currentUsername = securityUtils.getCurrentUserLogin();
        boolean isAdminOrReceptionist = securityUtils.hasRole("READ_INVOICE");
        boolean isPatient = securityUtils.hasRole("READ_OWN_INVOICE");

        Page<Invoice> pageData;

        if (isAdminOrReceptionist) {
            pageData = invoiceRepository.getAllInvoiceDetails(paymentStatus, paymentMethod, startDate, endDate, keyword, pageable);
        }
        else if (isPatient) {
            Patient patient = patientRepository.findByUser_Email(currentUsername)
                    .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

            pageData = invoiceRepository.findByAppointment_Patient_PatientId(patient.getPatientId(), pageable);
        }
        else {
            pageData = Page.empty();
        }

        return invoiceMapper.toInvoicePage(pageData);
    }

    public InvoiceResponse findByAppointmentId(Long appointmentId) {
        Invoice invoice = invoiceRepository.findByAppointment_AppointmentId(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        String currentUsername = securityUtils.getCurrentUserLogin();
        boolean canSeeAll = securityUtils.hasRole("READ_INVOICE");

        if (!canSeeAll) {
            String ownerEmail = invoice.getAppointment().getPatient().getUser().getEmail();
            if (!ownerEmail.equals(currentUsername)) {
                throw new AppException(ErrorCode.UNAUTHORIZED);
            }
        }
        return invoiceMapper.toInvoiceResponse(invoice);
    }

    @Transactional
    public void delete(Long invoiceId) {
        // 1. Tìm hóa đơn
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        // 2. Validate
        if (invoice.getPaymentStatus() == PaymentStatus.PAID) {
            throw new AppException(ErrorCode.CANNOT_DELETE_PAID_INVOICE);
        }

        // 3. [QUAN TRỌNG NHẤT] Cắt đứt quan hệ với Appointment
        // Vì Appointment trỏ ngược về Invoice, nên cần set null để Hibernate không bị lỗi
        Appointment appointment = invoice.getAppointment();
        if (appointment != null) {
            appointment.setInvoice(null);
            // Không cần gọi save(appointment) nếu đang trong @Transactional,
            // nhưng để chắc ăn bạn có thể thêm: appointmentRepository.save(appointment);
        }

        // 4. Tiến hành xóa Invoice
        // Lúc này các InvoiceDetail sẽ tự động xóa theo (do CascadeType.ALL ở Entity)
        invoiceRepository.delete(invoice);
    }
}
