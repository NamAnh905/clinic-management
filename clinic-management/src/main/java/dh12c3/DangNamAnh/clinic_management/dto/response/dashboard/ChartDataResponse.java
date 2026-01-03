package dh12c3.DangNamAnh.clinic_management.dto.response.dashboard;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)

public class ChartDataResponse {
    String label;
    BigDecimal value;
}