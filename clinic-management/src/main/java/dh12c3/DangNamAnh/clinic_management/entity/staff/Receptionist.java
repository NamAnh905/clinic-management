package dh12c3.DangNamAnh.clinic_management.entity.staff;

import dh12c3.DangNamAnh.clinic_management.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Entity
@Table(name = "receptionists")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class Receptionist {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long receptionistId;

    @OneToOne
    @JoinColumn(name = "userId", nullable = false, unique = true)
    User user;

    @Column(unique = true, nullable = false, length = 20)
    String employeeCode;

    @Column(nullable = false)
    LocalDate hireDate;
}