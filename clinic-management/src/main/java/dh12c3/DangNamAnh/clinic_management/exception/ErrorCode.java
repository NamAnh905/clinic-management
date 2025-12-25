package dh12c3.DangNamAnh.clinic_management.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    // ========================================================================
    // 1xxx: GLOBAL / SYSTEM ERRORS
    // ========================================================================
    UNCATEGORIZED_EXCEPTION(9999, "Unknown error.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid data.", HttpStatus.BAD_REQUEST),
    MISSING_PARAMETER(1002, "Missing required parameter.", HttpStatus.BAD_REQUEST),
    DATA_INVALID(1003, "Invalid or duplicate data.", HttpStatus.BAD_REQUEST),

    // ========================================================================
    // 2xxx: AUTHENTICATION & USER
    // ========================================================================
    UNAUTHENTICATED(2001, "Not logged in or invalid token.", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(2002, "No access permission.", HttpStatus.FORBIDDEN), // Sửa lại status code chuẩn
    USER_NOT_FOUND(2003, "User not found.", HttpStatus.NOT_FOUND),
    EXISTED_EMAIL(2004, "Email address already in use.", HttpStatus.CONFLICT),
    ROLE_NOT_FOUND(2005, "Role not found.", HttpStatus.NOT_FOUND),
    STAFF_ROLE_CHANGE_DENIED(2006, "Current role cannot be changed to avoid role conflict.",  HttpStatus.CONFLICT),
    INVALID_PASSWORD(2007, "Invalid password.", HttpStatus.UNAUTHORIZED),

    // ========================================================================
    // 3xxx: CATALOG (SERVICE, SPECIALTY, DRUG)
    // ========================================================================
    // Specialty
    SPECIALTY_NOT_FOUND(3001, "Specialty not found.", HttpStatus.NOT_FOUND),

    // Service
    SERVICE_NOT_FOUND(3101, "Service not found.", HttpStatus.NOT_FOUND),

    // Drug
    DRUG_NOT_FOUND(3201, "Drug not found.", HttpStatus.NOT_FOUND),
    DRUG_OOS(3202, "This drug is currently out of stock.", HttpStatus.CONFLICT),

    // ========================================================================
    // 4xxx: OPERATIONS (SCHEDULE, APPOINTMENT)
    // ========================================================================
    // Schedule
    SCHEDULE_NOT_FOUND(4001, "Schedule not found.", HttpStatus.NOT_FOUND),
    EXISTED_SCHEDULE(4002, "Working schedule existed.", HttpStatus.CONFLICT),
    DOCTOR_HAS_NO_WORKING_SCHEDULE(4003, "Doctor does not have working schedules today.", HttpStatus.BAD_REQUEST),
    CANNOT_CHANGE_SCHEDULE(4004, "Cannot change the schedule due to an active appointment.", HttpStatus.CONFLICT),

    // Appointment
    APPOINTMENT_NOT_FOUND(4101, "Appointment not found.", HttpStatus.NOT_FOUND),
    APPOINTMENT_ALREADY_BOOKED(4102, "The doctor already has an appointment at this time.", HttpStatus.CONFLICT),
    STATUS_CHANGE_NOT_ALLOWED(4103, "Current appointment status does not allow this transition.", HttpStatus.BAD_REQUEST),
    CANNOT_CANCEL_LATE(4104, "Appointment cannot be canceled within 24 hours of its scheduled time.", HttpStatus.BAD_REQUEST),
    PATIENT_HAS_HISTORY(4105, "Cannot upgrade to staff role due to existing records.", HttpStatus.BAD_REQUEST),
    // ========================================================================
    // 5xxx: RESULTS (RECORDS, INVOICE)
    // ========================================================================
    // Medical Records & Prescriptions
    RECORD_NOT_FOUND(5001, "Record not found.", HttpStatus.NOT_FOUND),
    PRESCRIPTION_NOT_FOUND(5002, "Prescription not found.", HttpStatus.NOT_FOUND),
    DETAIL_NOT_FOUND(5003, "Detail not found.", HttpStatus.NOT_FOUND),
    DRUG_NOT_IN_PRESCRIPTION(5004, "This drug is not included in the prescription.", HttpStatus.BAD_REQUEST),

    // Invoice
    INVOICE_NOT_FOUND(5101, "Invoice not found.", HttpStatus.NOT_FOUND),
    INVOICE_ALREADY_EXISTS(5102, "This appointment has already been invoiced.", HttpStatus.CONFLICT),
    CANNOT_CREATE_INVOICE(5103, "Cannot create invoice for an incomplete appointment.", HttpStatus.BAD_REQUEST),
    CANNOT_DELETE_PAID_INVOICE(5104, "Cannot delete a paid invoice.", HttpStatus.BAD_REQUEST),
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