package dh12c3.DangNamAnh.clinic_management.dto.request.master;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class SpecialtyCreationRequest {
    @NotBlank(message = "Specialty name cannot be empty.")
    String name;
    String description;
    Set<Long> doctors;
}
