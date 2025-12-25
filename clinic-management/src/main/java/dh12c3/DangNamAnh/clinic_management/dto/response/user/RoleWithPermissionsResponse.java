package dh12c3.DangNamAnh.clinic_management.dto.response.user;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class RoleWithPermissionsResponse {
    String name;
    Set<PermissionResponse> permissions;
}
