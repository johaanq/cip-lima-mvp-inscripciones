package pe.org.ciplima.backend.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pe.org.ciplima.backend.config.AppProperties;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AppProperties appProperties;
    private final PasswordEncoder passwordEncoder;
    private final String encodedPassword;

    public AdminUserDetailsService(AppProperties appProperties, PasswordEncoder passwordEncoder) {
        this.appProperties = appProperties;
        this.passwordEncoder = passwordEncoder;
        this.encodedPassword = passwordEncoder.encode(appProperties.admin().password());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (!appProperties.admin().username().equals(username)) {
            throw new UsernameNotFoundException("Usuario administrador no encontrado");
        }

        return User.builder()
                .username(appProperties.admin().username())
                .password(encodedPassword)
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                .build();
    }
}
