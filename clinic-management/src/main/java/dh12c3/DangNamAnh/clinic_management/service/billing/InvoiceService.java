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
import dh12c3.DangNamAnh.clinic_management.enums.InvoiceType;
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
import dh12c3.DangNamAnh.clinic_management.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.Map;
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
    VnPayService vnPayService;

    SecurityUtils securityUtils;

    // =========================================================================
    // 1. NGHIỆP VỤ PRE-PAID (THANH TOÁN PHÍ KHÁM TRƯỚC)
    // =========================================================================

    @Transactional
    public InvoiceResponse createBookingInvoice(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        // Chỉ cho phép tạo hóa đơn cọc khi đang PENDING (chưa khám)
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new AppException(ErrorCode.CANNOT_CREATE_INVOICE); // Hoặc lỗi INVALID_STATUS
        }

        // Lấy giá dịch vụ khám mặc định
        Specialty specialty = appointment.getDoctor().getSpecialty();
        ServiceEntity consultationService = specialty.getDefaultService();
        if (consultationService == null) {
            throw new RuntimeException("Chuyên khoa chưa cấu hình dịch vụ khám mặc định");
        }

        // Tạo Invoice
        Invoice invoice = new Invoice();
        invoice.setAppointment(appointment);
        invoice.setTransactionCode(AppUtils.generateTransactionCode());
        invoice.setPaymentStatus(PaymentStatus.PENDING);
        invoice.setPaymentMethod(PaymentMethod.VNPAY); // Mặc định là VNPAY
        invoice.setType(InvoiceType.BOOKING); // <--- QUAN TRỌNG

        // Set tổng tiền = Giá khám
        invoice.setTotalAmount(consultationService.getPrice());

        Invoice savedInvoice = invoiceRepository.save(invoice);

        // Lưu chi tiết hóa đơn (Chỉ có 1 dòng là Phí khám)
        InvoiceDetail detail = new InvoiceDetail();
        detail.setInvoice(savedInvoice);
        detail.setService(consultationService);
        detail.setQuantity(1);
        detail.setUnitPrice(consultationService.getPrice());
        invoiceDetailRepository.save(detail);

        return invoiceMapper.toInvoiceResponse(savedInvoice);
    }

    // =========================================================================
    // 2. NGHIỆP VỤ POST-PAID (THANH TOÁN SAU KHI KHÁM)
    // =========================================================================

    @Transactional
    public InvoiceResponse createFinalInvoice(InvoiceCreationRequest request) { // Đã đổi tên hàm
        Appointment appointment = appointmentRepository.findById(request.getAppointmentId())
                .orElseThrow(() -> new AppException(ErrorCode.APPOINTMENT_NOT_FOUND));

        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new AppException(ErrorCode.CANNOT_CREATE_INVOICE);
        }

        // Check xem đã có hóa đơn FINAL nào chưa (tránh tạo trùng)
        if (invoiceRepository.existsByAppointment_AppointmentIdAndTypeAndPaymentStatus(
                request.getAppointmentId(), InvoiceType.FINAL, PaymentStatus.PAID)) {
            throw new AppException(ErrorCode.INVOICE_ALREADY_EXISTS);
        }

        Invoice invoice = invoiceMapper.toInvoice(request);
        invoice.setAppointment(appointment);
        invoice.setTransactionCode(AppUtils.generateTransactionCode());
        invoice.setPaymentStatus(PaymentStatus.PENDING);
        invoice.setType(InvoiceType.FINAL); // <--- Set loại FINAL

        Invoice savedInvoice = invoiceRepository.save(invoice);
        BigDecimal totalAmount = BigDecimal.ZERO;

        // --- LOGIC MỚI: KIỂM TRA ĐÃ THANH TOÁN PHÍ KHÁM TRƯỚC CHƯA ---
        boolean hasPaidBooking = invoiceRepository.existsByAppointment_AppointmentIdAndTypeAndPaymentStatus(
                request.getAppointmentId(), InvoiceType.BOOKING, PaymentStatus.PAID
        );

        Specialty specialty = appointment.getDoctor().getSpecialty();
        ServiceEntity consultationService = specialty.getDefaultService();

        // Chỉ cộng tiền khám nếu CHƯA trả trước
        if (consultationService != null && !hasPaidBooking) {
            InvoiceDetail examDetail = new InvoiceDetail();
            examDetail.setInvoice(savedInvoice);
            examDetail.setService(consultationService);
            examDetail.setQuantity(1);
            examDetail.setUnitPrice(consultationService.getPrice());
            invoiceDetailRepository.save(examDetail);
            totalAmount = totalAmount.add(consultationService.getPrice());
        }

        // Cộng các dịch vụ phát sinh (CLS, XN...)
        if (request.getServiceIds() != null && !request.getServiceIds().isEmpty()) {
            List<ServiceEntity> services = serviceEntityRepository.findAllById(request.getServiceIds());
            for (ServiceEntity service : services) {
                // Skip nếu trùng dịch vụ khám (đã xử lý ở trên)
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

        // Cộng tiền thuốc
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
            updateDrugStock(invoice);
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
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        if (invoice.getPaymentStatus() == PaymentStatus.PAID) {
            throw new AppException(ErrorCode.CANNOT_DELETE_PAID_INVOICE);
        }

        Appointment appointment = invoice.getAppointment();
        if (appointment != null) {
            appointment.setInvoice(null);
        }
        invoiceRepository.delete(invoice);
    }

    // --- 1. HÀM TẠO URL THANH TOÁN (Logic nghiệp vụ) ---
    public String createVnPayUrl(Long invoiceId, HttpServletRequest request) {
        // Lấy hóa đơn từ DB
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        // Kiểm tra logic nghiệp vụ
        if (invoice.getPaymentStatus() == PaymentStatus.PAID) {
            throw new AppException(ErrorCode.INVOICE_ALREADY_PAID);
        }

        // Gọi VnPayService để lấy String URL (chỉ xử lý chuỗi)
        return vnPayService.createPaymentUrl(
                invoice.getTotalAmount().longValue(),
                invoice.getTransactionCode(),
                request
        );
    }

    // --- 2. HÀM XỬ LÝ KẾT QUẢ TRẢ VỀ (Logic nghiệp vụ + Trừ kho) ---
    @Transactional
    public InvoiceResponse processVnPayCallback(Map<String, String> queryParams) {
        if (!vnPayService.validateCallback(queryParams)) {
            throw new AppException(ErrorCode.INVALID_SIGNATURE);
        }

        String vnp_ResponseCode = queryParams.get("vnp_ResponseCode");
        String transactionCode = queryParams.get("vnp_TxnRef");

        Invoice invoice = invoiceRepository.findByTransactionCode(transactionCode)
                .orElseThrow(() -> new AppException(ErrorCode.INVOICE_NOT_FOUND));

        if ("00".equals(vnp_ResponseCode)) {
            if (invoice.getPaymentStatus() != PaymentStatus.PAID) {
                invoice.setPaymentStatus(PaymentStatus.PAID);
                invoice.setPaymentMethod(PaymentMethod.VNPAY);

                // --- LOGIC MỚI TẠI ĐÂY ---
                if (invoice.getType() == InvoiceType.BOOKING) {
                    // Nếu là thanh toán Booking -> Tự động xác nhận lịch hẹn
                    Appointment appt = invoice.getAppointment();
                    if (appt.getStatus() == AppointmentStatus.PENDING) {
                        appt.setStatus(AppointmentStatus.CONFIRMED);
                        appointmentRepository.save(appt);
                    }
                } else {
                    // Nếu là thanh toán Final -> Mới trừ kho thuốc
                    updateDrugStock(invoice);
                }

                return invoiceMapper.toInvoiceResponse(invoiceRepository.save(invoice));
            }
        } else {
            throw new AppException(ErrorCode.PAYMENT_FAILED);
        }
        return invoiceMapper.toInvoiceResponse(invoice);
    }

    // Hàm phụ trợ trừ kho (được tách ra từ logic update cũ để code gọn hơn)
    private void updateDrugStock(Invoice invoice) {
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
}
