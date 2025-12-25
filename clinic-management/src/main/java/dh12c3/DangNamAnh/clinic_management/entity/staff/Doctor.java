package dh12c3.DangNamAnh.clinic_management.entity.staff;

import dh12c3.DangNamAnh.clinic_management.entity.appointment.Appointment;
import dh12c3.DangNamAnh.clinic_management.entity.master.Specialty;
import dh12c3.DangNamAnh.clinic_management.entity.schedule.WorkingSchedule;
import dh12c3.DangNamAnh.clinic_management.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@Table(name = "Doctors")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Doctor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long doctorId;

    @OneToOne
    @JoinColumn(name = "userId", nullable = false, unique = true)
    User user;

    @Column(unique = true, nullable = false, length = 20)
    String employeeCode;

    @ManyToOne
    @JoinColumn(name = "specialtyId", nullable = false)
    Specialty specialty;

    @Column(nullable = false, unique = true, length = 100)
    String licenseNumber;

    @OneToMany(mappedBy = "doctor")
    Set<WorkingSchedule> workingSchedules;

    @OneToMany(mappedBy = "doctor")
    Set<Appointment> appointments;
}

