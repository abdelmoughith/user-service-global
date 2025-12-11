package pack.userservice.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import pack.userservice.dtos.AuthResponse;
import pack.userservice.dtos.LoginRequest;
import pack.userservice.dtos.RegisterRequest;
import pack.userservice.entities.Role;
import pack.userservice.entities.User;
import pack.userservice.exception.ValidationException;
import pack.userservice.security.JwtUtils;
import pack.userservice.services.CustomUserService;

import java.util.List;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserService customUserService;



    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        if (customUserService.existsByEmail(request.email())) {
            throw new ValidationException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        Role role = Role.STUDENT;
        user.setRole(role);

        customUserService.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        User user = customUserService.findByEmail(request.email());
        // Authentication authentication =
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        String jwt = jwtUtils.generateToken(user);
        return ResponseEntity.ok(new AuthResponse(jwt));
    }
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> all(){ return customUserService.getAllUsers(); }
}
