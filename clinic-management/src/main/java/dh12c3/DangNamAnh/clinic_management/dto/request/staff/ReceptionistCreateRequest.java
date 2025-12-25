package dh12c3.DangNamAnh.clinic_management.dto.request.staff;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class ReceptionistCreateRequest {
    @NotNull(message = "UserId cannot be empty.")
    Long userId;

    String employeeCode;

    @NotNull(message = "Hire date cannot be empty.")
    LocalDate hireDate;
}
