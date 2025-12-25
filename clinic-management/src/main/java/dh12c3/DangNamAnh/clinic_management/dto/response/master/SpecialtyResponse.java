package dh12c3.DangNamAnh.clinic_management.dto.response.master;

import dh12c3.DangNamAnh.clinic_management.dto.response.staff.DoctorResponse;
import dh12c3.DangNamAnh.clinic_management.helper.ExcelColumn;
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

public class SpecialtyResponse {
    Long specialtyId;
    String name;
    String description;
}
