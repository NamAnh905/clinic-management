package dh12c3.DangNamAnh.clinic_management.dto.response.medical;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PresDetailResponse {
    Long detailId;
    String drugName;
    String unit;
    Integer quantity;
    String dosage;
}
