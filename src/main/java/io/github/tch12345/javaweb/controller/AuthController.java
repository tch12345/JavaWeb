package io.github.tch12345.javaweb.controller;


import io.github.tch12345.javaweb.table.User;
import io.github.tch12345.javaweb.repository.UserRepository;
import io.github.tch12345.javaweb.service.AuthService;
import io.github.tch12345.javaweb.dto.LoginRequest;
import io.github.tch12345.javaweb.dto.RegisterRequest;
import io.github.tch12345.javaweb.dto.ApiResponse;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error("Email already exists!"));
        }
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole("USER");
        user.setName(request.getName());
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully!"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(LoginRequest request) {
        String email = request.getEmail();
        String password = request.getPassword();

        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("User not found"));
        }

        User user = userOptional.get();
        if (!user.getPassword().equals(password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Incorrect password"));
        }
        String token=authService.generateToken(user);

        return ResponseEntity.ok(ApiResponse.success("Login successful", token));
    }

}
