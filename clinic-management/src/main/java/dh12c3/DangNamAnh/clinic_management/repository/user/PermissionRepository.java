package dh12c3.DangNamAnh.clinic_management.repository.user;

import dh12c3.DangNamAnh.clinic_management.entity.user.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, String> {
}
