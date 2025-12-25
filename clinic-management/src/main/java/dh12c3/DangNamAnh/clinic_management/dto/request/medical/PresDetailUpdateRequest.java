package dh12c3.DangNamAnh.clinic_management.dto.request.medical;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PresDetailUpdateRequest {
    Integer quantity;
    String dosage;
}
