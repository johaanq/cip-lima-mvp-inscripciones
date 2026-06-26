package pe.org.ciplima.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import pe.org.ciplima.backend.domain.entity.AdminUsuario;
import pe.org.ciplima.backend.repository.AdminUsuarioRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserDetailsServiceTest {

    @Mock
    private AdminUsuarioRepository adminUsuarioRepository;

    private AdminUserDetailsService adminUserDetailsService;
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        adminUserDetailsService = new AdminUserDetailsService(adminUsuarioRepository);
        passwordEncoder = new BCryptPasswordEncoder();
    }

    @Test
    void cargaAdminActivoDesdeBaseDeDatos() {
        AdminUsuario admin = new AdminUsuario("admin", passwordEncoder.encode("admin123"));
        when(adminUsuarioRepository.findByUsernameAndActivoTrue("admin")).thenReturn(Optional.of(admin));

        UserDetails userDetails = adminUserDetailsService.loadUserByUsername("admin");

        assertEquals("admin", userDetails.getUsername());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(passwordEncoder.matches("admin123", userDetails.getPassword()));
    }

    @Test
    void rechazaUsuarioInexistente() {
        when(adminUsuarioRepository.findByUsernameAndActivoTrue("otro")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> adminUserDetailsService.loadUserByUsername("otro"));
    }
}
