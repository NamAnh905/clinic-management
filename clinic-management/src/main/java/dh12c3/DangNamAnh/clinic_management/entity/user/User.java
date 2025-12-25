package dh12c3.DangNamAnh.clinic_management.entity.user;

import dh12c3.DangNamAnh.clinic_management.entity.patient.Patient;
import dh12c3.DangNamAnh.clinic_management.entity.staff.Doctor;
import dh12c3.DangNamAnh.clinic_management.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "Users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long userId;

    @Column(nullable = false, unique = true, length = 100)
    String email;

    @Column(nullable = false, length = 255)
    String passwordHash;

    @Column(nullable = false, length = 100)
    String fullName;

    @Column(unique = true, length = 20)
    String phoneNumber;

    @Column(length = 255)
    String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    Gender gender;

    @Column(nullable = false)
    LocalDate dateOfBirth;

    @ManyToMany
    Set<Role> roles;

    @Column(nullable = false)
    boolean isActive;

    @OneToOne(mappedBy = "user")
    Doctor doctor;

    @OneToOne(mappedBy = "user")
    Patient patient;
}