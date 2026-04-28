package com.andrei.demo.service;

import com.andrei.demo.config.JwtService;
import com.andrei.demo.model.LoginResponse;
import com.andrei.demo.model.Person;
import com.andrei.demo.repository.PersonRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class SecurityService {
    private final PersonRepository personRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(String email, String password) {
        Optional<Person> maybePerson = personRepository.findByEmail(email);

        if (maybePerson.isEmpty()) {
            return new LoginResponse("Person with email " + email + " not found");
        }

        Person person = maybePerson.get();
        if (passwordEncoder.matches(password, person.getPassword())) {
            // Ensure we get the name of the role (admin, student, etc.)
            // If role is null, we default to "student" (safer than admin)
            String roleName = (person.getRole() != null) ? person.getRole().name() : "student";

            // Generate token using the unified JwtService
            String token = jwtService.generateToken(email, roleName);

            // Extract the ID
            String userId = person.getId() != null ? person.getId().toString() : null;

            // Return the successful response
            return new LoginResponse(roleName, token, userId);
        } else {
            return new LoginResponse("Incorrect password");
        }
    }
}