package dh12c3.DangNamAnh.clinic_management.repository.user;

import dh12c3.DangNamAnh.clinic_management.entity.user.User;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long>, JpaSpecificationExecutor<User> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Override
    @EntityGraph(attributePaths = "roles")
    Page<User> findAll(@Nullable Specification<User> spec,@NonNull Pageable pageable);
}
