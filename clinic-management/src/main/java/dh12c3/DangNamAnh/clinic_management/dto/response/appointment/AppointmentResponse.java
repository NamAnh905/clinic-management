package dh12c3.DangNamAnh.clinic_management.dto.response.appointment;

import com.fasterxml.jackson.annotation.JsonFormat;
import dh12c3.DangNamAnh.clinic_management.enums.AppointmentStatus;
import dh12c3.DangNamAnh.clinic_management.helper.ExcelColumn;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class AppointmentResponse {
    @ExcelColumn(name = "ID cuộc hẹn")
    Long appointmentId;

    @ExcelColumn(name = "ID bệnh nhân")
    Long patientId;

    @ExcelColumn(name = "Tên bệnh nhân")
    String patientName;

    @ExcelColumn(name = "ID bác sĩ")
    Long doctorId;

    @ExcelColumn(name = "Tên bác sĩ")
    String doctorName;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @ExcelColumn(name = "Thời gian hẹn")
    LocalDateTime appointmentTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @ExcelColumn(name = "Kết thúc")
    LocalDateTime endTime;

    @ExcelColumn(name = "Lý do khám")
    String reason;

    @ExcelColumn(name = "Trạng thái")
    AppointmentStatus status;
}
