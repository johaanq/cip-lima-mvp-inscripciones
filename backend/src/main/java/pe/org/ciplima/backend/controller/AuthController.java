package pe.org.ciplima.backend.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.org.ciplima.backend.dto.LoginRequest;
import pe.org.ciplima.backend.dto.LoginResponse;
import pe.org.ciplima.backend.security.JwtService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacion", description = "Login del administrador")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (AuthenticationException ex) {
            throw new IllegalArgumentException("Credenciales administrativas inválidas");
        }

        String token = jwtService.generateToken(request.username());
        return LoginResponse.of(token, jwtService.getExpirationMs());
    }
}
