package com.andrei.demo.service;

import com.andrei.demo.config.JwtService;
import com.andrei.demo.model.LoginResponse;
import com.andrei.demo.model.Person;
import com.andrei.demo.repository.PersonRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.andrei.demo.model.Role;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SecurityServiceTests {

    @Mock
    private PersonRepository personRepository;

    // NEW: must be mocked so @InjectMocks can inject all constructor dependencies
    @Mock
    private JwtService jwtService;

    // NEW: must be mocked so @InjectMocks can inject all constructor dependencies
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SecurityService securityService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testLoginSuccess() {
        String email = "john@example.com";
        String rawPassword = "password";
        String hashedPassword = "$2a$10$hashedvalue";
        String fakeToken = "jwt.token.here";

        Person person = new Person();
        person.setEmail(email);
        person.setPassword(hashedPassword);
        person.setRole(Role.admin);

        // passwordEncoder.matches(rawPassword, hashedPassword) -> true
        when(personRepository.findByEmail(email)).thenReturn(Optional.of(person));
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(true);
        when(jwtService.generateToken(email, "admin")).thenReturn(fakeToken);

        LoginResponse result = securityService.login(email, rawPassword);

        assertTrue(result.success());
        assertEquals("admin", result.role());
        assertEquals(fakeToken, result.token());
        assertNull(result.errorMessage());
        verify(personRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(rawPassword, hashedPassword);
        verify(jwtService, times(1)).generateToken(email, "admin");
    }

    @Test
    void testLoginIncorrectPassword() {
        String email = "john@example.com";
        String rawPassword = "wrongpassword";
        String hashedPassword = "$2a$10$hashedvalue";

        Person person = new Person();
        person.setEmail(email);
        person.setPassword(hashedPassword);

        when(personRepository.findByEmail(email)).thenReturn(Optional.of(person));
        // passwordEncoder.matches with wrong password -> false
        when(passwordEncoder.matches(rawPassword, hashedPassword)).thenReturn(false);

        LoginResponse result = securityService.login(email, rawPassword);

        assertFalse(result.success());
        assertEquals("Incorrect password", result.errorMessage());
        assertNull(result.token());
        verify(personRepository, times(1)).findByEmail(email);
        verify(passwordEncoder, times(1)).matches(rawPassword, hashedPassword);
        // Token must NOT be generated on failed login
        verify(jwtService, never()).generateToken(any(), any());
    }

    @Test
    void testLoginEmailNotFound() {
        String email = "john@example.com";
        String password = "password";

        when(personRepository.findByEmail(email)).thenReturn(Optional.empty());

        LoginResponse result = securityService.login(email, password);

        assertFalse(result.success());
        assertEquals("Person with email " + email + " not found", result.errorMessage());
        assertNull(result.token());
        verify(personRepository, times(1)).findByEmail(email);
        // No password check or token generation when user not found
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any(), any());
    }
}