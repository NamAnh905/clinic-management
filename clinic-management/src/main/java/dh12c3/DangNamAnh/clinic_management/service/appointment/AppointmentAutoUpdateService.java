package dh12c3.DangNamAnh.clinic_management.service.appointment;

import dh12c3.DangNamAnh.clinic_management.entity.appointment.Appointment;
import dh12c3.DangNamAnh.clinic_management.enums.AppointmentStatus;
import dh12c3.DangNamAnh.clinic_management.repository.appoinment.AppointmentRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AppointmentAutoUpdateService {

    AppointmentRepository appointmentRepository;

    @Scheduled(cron = "0 */5 * * * *")
    @Transactional
    public void scanAndMarkNoShow() {
        LocalDateTime cutOffTime = LocalDateTime.now().minusMinutes(30);

        List<AppointmentStatus> targetStatuses = List.of(
                AppointmentStatus.PENDING,
                AppointmentStatus.CONFIRMED
        );

        List<Appointment> overdueAppointments = appointmentRepository.findByStatusInAndAppointmentTimeBefore(
                targetStatuses,
                cutOffTime
        );

        if (overdueAppointments.isEmpty()) return;

        log.info("System Scan: Tìm thấy {} lịch hẹn (PENDING/CONFIRMED) quá hạn.", overdueAppointments.size());

        for (Appointment appt : overdueAppointments) {
            AppointmentStatus oldStatus = appt.getStatus();

            appt.setStatus(AppointmentStatus.NO_SHOW);

            String autoReason = String.format("Hệ thống: Khách vắng mặt (Trạng thái cũ: %s, Quá giờ 30p)", oldStatus);

            if (appt.getReason() != null && !appt.getReason().isEmpty()) {
                appt.setReason(appt.getReason() + " | " + autoReason);
            } else {
                appt.setReason(autoReason);
            }
        }

        appointmentRepository.saveAll(overdueAppointments);
        log.info("System Scan: Đã cập nhật xong.");
    }
}