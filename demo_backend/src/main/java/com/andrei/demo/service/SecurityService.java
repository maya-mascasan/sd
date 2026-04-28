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
            // This uses your single-argument constructor: LoginResponse(String errorMessage)
            return new LoginResponse("Person with email " + email + " not found");
        }

        Person person = maybePerson.get();
        if (passwordEncoder.matches(password, person.getPassword())) {
            String roleName = (person.getRole() != null) ? person.getRole().name() : "admin";
            String token = jwtService.generateToken(email, roleName);

            // Extract the ID as a String
            String userId = person.getId() != null ? person.getId().toString() : null;

            // This uses your three-argument constructor: LoginResponse(String role, String token, String userId)
            return new LoginResponse(roleName, token, userId);
        } else {
            // Using the error constructor again
            return new LoginResponse("Incorrect password");
        }
    }
}