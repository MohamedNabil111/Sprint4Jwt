package com.bolla.sprint4hags.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "username is required")
    @Size(min = 3, max = 50, message = "username must be between 3 and 50 characters.")
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 6, message = "password must be at least 6 characters.")
    private String password;

    private String role = "USER";
}
