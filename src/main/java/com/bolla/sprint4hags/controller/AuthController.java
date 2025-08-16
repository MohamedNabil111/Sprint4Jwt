package com.bolla.sprint4hags.controller;

import com.bolla.sprint4hags.dto.AuthRequest;
import com.bolla.sprint4hags.dto.AuthResponse;
import com.bolla.sprint4hags.dto.RegisterRequest;
import com.bolla.sprint4hags.model.User;
import com.bolla.sprint4hags.security.JwtUtil;
import com.bolla.sprint4hags.service.TokenBlacklistService;
import com.bolla.sprint4hags.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.parser.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserService userService;
    @Autowired
    private TokenBlacklistService  tokenBlacklistService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            authRequest.getUsername(),
                            authRequest.getPassword()
                    )
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
            String jwt = jwtUtil.generateToken(userDetails);
            return ResponseEntity.ok(new AuthResponse(jwt, "Login successful", userDetails.getUsername()));
        }
        catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, "Invalid username or password", null));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(null, "Authentication failed: " + e.getMessage(), null));
        }
    }

    @PostMapping("/register")
    public  ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            if(userService.existsByUsername(registerRequest.getUsername())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new AuthResponse(null, "Username already exists", null));
            }

            String requestedRole = registerRequest.getRole();
            if (requestedRole != null && !requestedRole.equals("USER") && !requestedRole.equals("ADMIN")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new AuthResponse(null, "Invalid role specified", null));
            }

            User newUser = userService.createUser(registerRequest);
            UserDetails userDetails = userDetailsService.loadUserByUsername(newUser.getUsername());
            String jwt = jwtUtil.generateToken(userDetails);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthResponse(jwt, "User registered successfully with role: " + newUser.getRole(), newUser.getUsername()));
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(null, "Registration failed: " + e.getMessage(), null));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String AuthHeader) {
        try {
            if(AuthHeader !=  null && AuthHeader.startsWith("Bearer ")) {
                String token = AuthHeader.substring(7);
                String username = jwtUtil.extractUsername(token);

                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if(jwtUtil.validateToken(token, userDetails)) {
                    return ResponseEntity.ok(new AuthResponse(token, "Token validated successfully", username));
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, "Invalid token", null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, "Token validation failed", null));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                if (!tokenBlacklistService.isTokenBlacklisted(token)) {
                    tokenBlacklistService.blacklistToken(token);
                    return ResponseEntity.ok(new AuthResponse(null, "Logged out successfully", null));
                } else {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(new AuthResponse(null, "Already logged out", null));
                }
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new AuthResponse(null, "No valid token provided", null));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(null, "Logout failed: " + e.getMessage(), null));
        }
    }
}
