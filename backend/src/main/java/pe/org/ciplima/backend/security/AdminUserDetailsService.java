package pe.org.ciplima.backend.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pe.org.ciplima.backend.domain.entity.AdminUsuario;
import pe.org.ciplima.backend.repository.AdminUsuarioRepository;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUsuarioRepository adminUsuarioRepository;

    public AdminUserDetailsService(AdminUsuarioRepository adminUsuarioRepository) {
        this.adminUsuarioRepository = adminUsuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminUsuario admin = adminUsuarioRepository.findByUsernameAndActivoTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario administrador no encontrado"));

        return User.builder()
                .username(admin.getUsername())
                .password(admin.getPasswordHash())
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                .build();
    }
}
