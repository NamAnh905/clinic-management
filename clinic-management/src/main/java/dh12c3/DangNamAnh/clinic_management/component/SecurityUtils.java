package dh12c3.DangNamAnh.clinic_management.component;

import dh12c3.DangNamAnh.clinic_management.exception.AppException;
import dh12c3.DangNamAnh.clinic_management.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class SecurityUtils {
    public String getCurrentUserLogin(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
        return authentication.getName();
    }

    public boolean hasRole(String... roles){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return authentication.getAuthorities().stream()
                .anyMatch(authority -> {
                    String roleName = authority.getAuthority();

                    if("FULL_ACCESS".equals(roleName)){return true;}
                    for (String role :roles){
                        if (roleName.equals(role)){return true;}
                    }
                    return false;
                });
    }

    public void validateAccess(String ownerEmail, String... allowedRoles){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName();

        boolean hasPrivilegedRole = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> {
                    String authority = grantedAuthority.getAuthority();
                    if("FULL_ACCESS".equals(authority)){return true;}
                    return Arrays.asList(allowedRoles).contains(authority);
                });

        if (hasPrivilegedRole){
            return;
        }

        if (!currentEmail.equals(ownerEmail)) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }
    }
}
