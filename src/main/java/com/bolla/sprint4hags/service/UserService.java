package com.bolla.sprint4hags.service;

import com.bolla.sprint4hags.dto.RegisterRequest;
import com.bolla.sprint4hags.model.Role;
import com.bolla.sprint4hags.model.User;
import com.bolla.sprint4hags.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Transactional
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public User createUser(RegisterRequest registerRequest) {
        if(existsByUsername(registerRequest.getUsername())) {
            throw new RuntimeException("Username already exists: " +  registerRequest.getUsername());
        }
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));

        if (registerRequest.getRole() != null) {
            user.setRole(Role.valueOf(registerRequest.getRole().toUpperCase()));
        } else {
            user.setRole(Role.USER); // Default
        }

        return userRepository.save(user);
    }

//    public User createUser(String username, String password) {
//        if(existsByUsername(username)) {
//            throw new RuntimeException("Username already exists: " +  username);
//        }
//        User user = new User();
//        user.setUsername(username);
//        user.setPassword(passwordEncoder.encode(password));
//
//        return userRepository.save(user);
//    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Username not found: " +  username));
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updatePassword(String username, String newPassword) {
        User user = getUserByUsername(username);
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }

    public void deleteUserById(Long id) {
        if(!userRepository.existsById(id)) {
            throw new RuntimeException("User not found: " +  id);
        }
        userRepository.deleteById(id);
    }

    public void deleteUserByUsername(String username) {
        if(!existsByUsername(username)) {
            throw new RuntimeException("Username not found: " +  username);
        }
        userRepository.deleteByUsername(username);
    }
}
