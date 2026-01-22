package com.example.fitnessapp.Controller;

import com.example.fitnessapp.DTOs.LoginResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /**
     * Validiert die Basic Authentication Credentials
     * Wird vom Frontend verwendet, um zu prüfen, ob die Anmeldedaten korrekt sind
     */
    @GetMapping("/validate")
    public LoginResponse validate() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : null;
        
        return LoginResponse.builder()
                .username(username != null ? username : "")
                .message("Authentifizierung erfolgreich")
                .build();
    }
}
