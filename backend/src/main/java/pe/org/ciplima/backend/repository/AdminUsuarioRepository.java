package pe.org.ciplima.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.org.ciplima.backend.domain.entity.AdminUsuario;

import java.util.Optional;

public interface AdminUsuarioRepository extends JpaRepository<AdminUsuario, Long> {

    Optional<AdminUsuario> findByUsernameAndActivoTrue(String username);
}
