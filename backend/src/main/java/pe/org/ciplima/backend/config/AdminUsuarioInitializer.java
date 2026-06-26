package pe.org.ciplima.backend.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.org.ciplima.backend.domain.entity.AdminUsuario;
import pe.org.ciplima.backend.repository.AdminUsuarioRepository;

@Component
public class AdminUsuarioInitializer {

    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin123";

    private final AdminUsuarioRepository adminUsuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUsuarioInitializer(
            AdminUsuarioRepository adminUsuarioRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.adminUsuarioRepository = adminUsuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seedDefaultAdminIfMissing() {
        if (adminUsuarioRepository.count() == 0) {
            adminUsuarioRepository.save(new AdminUsuario(
                    DEFAULT_USERNAME,
                    passwordEncoder.encode(DEFAULT_PASSWORD)
            ));
        }
    }
}
