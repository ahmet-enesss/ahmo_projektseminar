package com.example.fitnessapp.Service;

import com.example.fitnessapp.DTOs.LoginRequest;
import com.example.fitnessapp.DTOs.LoginResponse;
import com.example.fitnessapp.Model.User;
import com.example.fitnessapp.Repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final UserRepository userRepository;
    // Speichert gültige Tokens (in Produktion sollte dies durch eine Datenbank oder Redis ersetzt werden)
    private final Map<String, String> validTokens = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                        "Benutzername oder Passwort ist falsch"));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                    "Benutzername oder Passwort ist falsch");
        }

        // Erstelle einen einfachen Token (UUID)
        String token = UUID.randomUUID().toString();
        
        // Speichere Token mit Benutzername
        validTokens.put(token, user.getUsername());

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .message("Anmeldung erfolgreich")
                .build();
    }

    public boolean validateToken(String token) {
        return token != null && validTokens.containsKey(token);
    }

    /**
     * Gibt den Benutzernamen für ein gültiges Token zurück
     * @param token Das Authentifizierungstoken
     * @return Der Benutzername oder null, wenn das Token ungültig ist
     */
    public String getUsernameFromToken(String token) {
        if (token == null || !validTokens.containsKey(token)) {
            return null;
        }
        return validTokens.get(token);
    }

    public void invalidateToken(String token) {
        validTokens.remove(token);
    }
}
