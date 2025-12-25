package dh12c3.DangNamAnh.clinic_management.dto.request.auth;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)

public class AuthenticationRequest {
    String email;
    String password;
}
