package dh12c3.DangNamAnh.clinic_management.entity.schedule;

import dh12c3.DangNamAnh.clinic_management.entity.staff.Doctor;
import dh12c3.DangNamAnh.clinic_management.entity.staff.Receptionist;
import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "WorkingSchedules")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WorkingSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long scheduleId;

    @ManyToOne
    @JoinColumn(name = "doctorId")
    Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "receptionistId")
    Receptionist receptionist;


    @Column(nullable = false)
    LocalDate workDate;

    @Column(nullable = false)
    LocalTime startTime;

    @Column(nullable = false)
    LocalTime endTime;

    boolean deleted = false;

    @AssertTrue(message = "Lịch phải thuộc về Bác sĩ HOẶC Lễ tân")
    public boolean isValidOwner() {
        return (doctor != null && receptionist == null) || (doctor == null && receptionist != null);
    }
}
