package com.example.fitnessapp.DTOs;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Benutzername ist erforderlich")
    private String username;

    @NotBlank(message = "Passwort ist erforderlich")
    private String password;
}
