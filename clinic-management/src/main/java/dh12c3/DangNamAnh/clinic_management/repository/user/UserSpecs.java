package dh12c3.DangNamAnh.clinic_management.repository.user;

import dh12c3.DangNamAnh.clinic_management.entity.user.Role;
import dh12c3.DangNamAnh.clinic_management.entity.user.User;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class UserSpecs {
    public static Specification<User> hasRole(String roleName){
        return (root, q, cb) -> {
            if (!StringUtils.hasText(roleName)) return null;
            Join<User, Role> rolesJoin = root.join("roles");
            return cb.equal(rolesJoin.get("name"), roleName);
        };
    }

    public static Specification<User> containsKeyword(String keyword){
        return (root, q, cb) -> {
            if (!StringUtils.hasText(keyword)) return null;
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("fullName")), pattern),
                    cb.like(cb.lower(root.get("address")), pattern),
                    cb.like(cb.lower(root.get("email")), pattern),
                    cb.like(cb.lower(root.get("phoneNumber")), pattern)
            );
        };
    }

    public static Specification<User> hasStatus(Boolean status){
        return (root, q, cb) -> {
            if (status == null) return null;
            return cb.equal(root.get("isActive"), status);
        };
    }
}
