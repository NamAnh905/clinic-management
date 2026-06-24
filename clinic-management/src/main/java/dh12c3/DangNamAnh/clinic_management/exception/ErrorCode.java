package dh12c3.DangNamAnh.clinic_management.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    // ========================================================================
    // 1xxx: GLOBAL / SYSTEM ERRORS
    // ========================================================================
    UNCATEGORIZED_EXCEPTION(9999, "Đã có lỗi hệ thống xảy ra, vui lòng thử lại sau.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Dữ liệu không hợp lệ.", HttpStatus.BAD_REQUEST),
    MISSING_PARAMETER(1002, "Thiếu tham số bắt buộc.", HttpStatus.BAD_REQUEST),
    DATA_INVALID(1003, "Dữ liệu không hợp lệ hoặc bị trùng lặp.", HttpStatus.BAD_REQUEST),

    // ========================================================================
    // 2xxx: AUTHENTICATION & USER
    // ========================================================================
    UNAUTHENTICATED(2001, "Chưa đăng nhập hoặc phiên đăng nhập đã hết hạn.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(2002, "Bạn không có quyền truy cập tính năng này.", HttpStatus.FORBIDDEN),
    USER_NOT_FOUND(2003, "Không tìm thấy thông tin tài khoản.", HttpStatus.NOT_FOUND),
    EXISTED_EMAIL(2004, "Địa chỉ email này đã được sử dụng, vui lòng chọn email khác.", HttpStatus.CONFLICT),
    ROLE_NOT_FOUND(2005, "Không tìm thấy vai trò (Role) này trên hệ thống.", HttpStatus.NOT_FOUND),
    STAFF_ROLE_CHANGE_DENIED(2006, "Không thể thay đổi vai trò hiện tại để tránh xung đột quyền hạn.",
            HttpStatus.CONFLICT),
    INVALID_PASSWORD(2007, "Mật khẩu không chính xác.", HttpStatus.UNAUTHORIZED),
    USER_LOCKED(2008, "Tài khoản của bạn đã bị khóa hoặc vô hiệu hóa.", HttpStatus.FORBIDDEN),
    INVALID_INFORMATION(2009, "Sai tên đăng nhập hoặc mật khẩu.", HttpStatus.UNAUTHORIZED),

    // ========================================================================
    // 3xxx: CATALOG (SERVICE, SPECIALTY, DRUG)
    // ========================================================================
    // Specialty
    SPECIALTY_NOT_FOUND(3001, "Không tìm thấy chuyên khoa.", HttpStatus.NOT_FOUND),

    // Service
    SERVICE_NOT_FOUND(3101, "Không tìm thấy dịch vụ.", HttpStatus.NOT_FOUND),

    // Drug
    DRUG_NOT_FOUND(3201, "Không tìm thấy thông tin thuốc.", HttpStatus.NOT_FOUND),
    DRUG_OOS(3202, "Loại thuốc này hiện đang tạm hết hàng.", HttpStatus.CONFLICT),

    // ========================================================================
    // 4xxx: OPERATIONS (SCHEDULE, APPOINTMENT)
    // ========================================================================
    // Schedule
    SCHEDULE_NOT_FOUND(4001, "Không tìm thấy lịch làm việc.", HttpStatus.NOT_FOUND),
    EXISTED_SCHEDULE(4002, "Lịch làm việc này đã tồn tại.", HttpStatus.CONFLICT),
    DOCTOR_HAS_NO_WORKING_SCHEDULE(4003, "Bác sĩ không có lịch làm việc vào ngày này.", HttpStatus.BAD_REQUEST),
    CANNOT_CHANGE_SCHEDULE(4004, "Không thể thay đổi lịch làm việc vì bác sĩ đã có lịch hẹn với bệnh nhân.",
            HttpStatus.CONFLICT),

    // Appointment
    APPOINTMENT_NOT_FOUND(4101, "Không tìm thấy lịch hẹn.", HttpStatus.NOT_FOUND),
    APPOINTMENT_ALREADY_BOOKED(4102, "Bác sĩ đã kín lịch vào khung giờ này, vui lòng chọn giờ khác.",
            HttpStatus.CONFLICT),
    STATUS_CHANGE_NOT_ALLOWED(4103, "Không thể thay đổi trạng thái của lịch hẹn này.", HttpStatus.BAD_REQUEST),
    CANNOT_CANCEL_LATE(4104, "Không thể hủy lịch hẹn khi quá sát giờ khám.", HttpStatus.BAD_REQUEST),
    PATIENT_HAS_HISTORY(4105, "Không thể cấp quyền nhân viên vì tài khoản này đã có lịch sử khám bệnh.",
            HttpStatus.BAD_REQUEST),
    PATIENT_TIME_CONFLICT(4106, "Bạn đã có lịch hẹn khác vào thời gian này. Vui lòng chọn khung giờ khác.",
            HttpStatus.CONFLICT),
    APPOINTMENT_TIME_NOT_ARRIVED(4107, "Không thể tạo bệnh án.", HttpStatus.CONFLICT),
    INVALID_TIME(4108, "Thời gian đặt lịch không hợp lệ. Vui lòng chọn thời gian cách hiện tại ít nhất 15 phút.",
            HttpStatus.BAD_REQUEST),
    INVALID_APPOINTMENT_SLOT(4109, "Thời gian đặt lịch phải là bội số 30 phút (ví dụ: 08:00, 08:30, 09:00...).",
            HttpStatus.BAD_REQUEST),
    DUPLICATE_SPECIALTY_SAME_DAY(4110, "Bạn đã có lịch hẹn của khoa này trong ngày hôm nay.", HttpStatus.CONFLICT),

    // ========================================================================
    // 5xxx: RESULTS (RECORDS, INVOICE)
    // ========================================================================
    // Medical Records & Prescriptions
    RECORD_NOT_FOUND(5001, "Không tìm thấy hồ sơ bệnh án.", HttpStatus.NOT_FOUND),
    PRESCRIPTION_NOT_FOUND(5002, "Không tìm thấy đơn thuốc.", HttpStatus.NOT_FOUND),
    DETAIL_NOT_FOUND(5003, "Không tìm thấy chi tiết thông tin.", HttpStatus.NOT_FOUND),
    DRUG_NOT_IN_PRESCRIPTION(5004, "Loại thuốc này không có trong đơn thuốc.", HttpStatus.BAD_REQUEST),

    // Invoice
    INVOICE_NOT_FOUND(5101, "Không tìm thấy hóa đơn.", HttpStatus.NOT_FOUND),
    INVOICE_ALREADY_EXISTS(5102, "Lịch hẹn này đã được xuất hóa đơn.", HttpStatus.CONFLICT),
    CANNOT_CREATE_INVOICE(5103, "Không thể xuất hóa đơn cho lịch hẹn chưa hoàn thành.", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_PAID_INVOICE(5104, "Không thể xóa hóa đơn đã thanh toán thành công.", HttpStatus.BAD_REQUEST),
    INVOICE_ALREADY_PAID(5105, "Hóa đơn này đã được thanh toán.", HttpStatus.BAD_REQUEST),
    INVALID_SIGNATURE(5106, "Chữ ký thanh toán không hợp lệ.", HttpStatus.BAD_REQUEST),
    PAYMENT_FAILED(5107, "Giao dịch thanh toán thất bại hoặc đã bị hủy.", HttpStatus.BAD_REQUEST),
    ;

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}