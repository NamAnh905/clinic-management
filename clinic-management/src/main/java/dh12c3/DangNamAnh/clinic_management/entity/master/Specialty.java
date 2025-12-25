package dh12c3.DangNamAnh.clinic_management.entity.master;

import dh12c3.DangNamAnh.clinic_management.entity.staff.Doctor;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Entity
@Table(name = "Specialties")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Specialty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long specialtyId;

    @Column(nullable = false, unique = true, length = 100)
    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    boolean deleted = false;

    @OneToMany(mappedBy = "specialty")
    Set<Doctor> doctors;

    @OneToOne
    @JoinColumn(name = "default_service_id")
    private ServiceEntity defaultService;
}
