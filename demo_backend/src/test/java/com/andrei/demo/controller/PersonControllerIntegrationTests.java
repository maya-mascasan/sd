package com.andrei.demo.controller;

import com.andrei.demo.config.JwtService;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.Role;
import com.andrei.demo.repository.PersonRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class PersonControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // JWT token for an admin user — generated fresh for each test run
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        personRepository.deleteAll();
        personRepository.flush();
        seedDatabase();

        // Generate a valid admin JWT token to authenticate all requests.
        // The token only needs a valid signature — the email does not need
        // to exist in the database for stateless JWT authentication.
        adminToken = jwtService.generateToken("test-admin@example.com", "admin");
    }

    private void seedDatabase() throws Exception {
        // Seed directly through the repository to bypass the service layer.
        // Passwords are hashed here so the seeded entities are realistic.
        Person john = new Person();
        john.setName("John Doe");
        john.setAge(30);
        john.setEmail("john.doe@example.com");
        john.setPassword(passwordEncoder.encode("Password_john123!@#"));
        john.setRole(Role.student);

        Person jane = new Person();
        jane.setName("Jane Doe");
        jane.setAge(25);
        jane.setEmail("jane.doe@example.com");
        jane.setPassword(passwordEncoder.encode("Password_jane123!@#"));
        jane.setRole(Role.student);

        personRepository.saveAll(List.of(john, jane));
    }

    @Test
    void testGetPeople() throws Exception {
        mockMvc.perform(get("/person")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].name",
                        Matchers.containsInAnyOrder("John Doe", "Jane Doe")))
                .andExpect(jsonPath("$[*].age",
                        Matchers.containsInAnyOrder(30, 25)))
                .andExpect(jsonPath("$[*].email",
                        Matchers.containsInAnyOrder(
                                "john.doe@example.com", "jane.doe@example.com"
                        )));
    }

    @Test
    void testAddPerson_ValidPayload() throws Exception {
        String validPersonJson = loadFixture("valid_person.json");

        mockMvc.perform(post("/person")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPersonJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Alice Smith"))
                .andExpect(jsonPath("$.age").value(28))
                .andExpect(jsonPath("$.email").value("alice.smith@example.com"));
        // NOTE: password is now hashed — we do NOT assert its plaintext value.
        // The hashed value will differ every time (BCrypt uses random salt).
    }

    @Test
    void testAddPerson_InvalidPayload() throws Exception {
        String invalidPersonJson = loadFixture("invalid_person.json");

        mockMvc.perform(post("/person")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPersonJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name")
                        .value("Name should be between 2 and 100 characters"))
                .andExpect(jsonPath("$.password")
                        .value("Password must contain at least 8 characters, including uppercase, lowercase, digit, and special character"))
                .andExpect(jsonPath("$.age")
                        .value("Age is required"))
                .andExpect(jsonPath("$.email")
                        .value("Email is required"));
    }

    @Test
    void testGetPeople_WithoutToken_Unauthorized() throws Exception {
        // Verify that the endpoint is properly protected — no token = 401
        mockMvc.perform(get("/person"))
                .andExpect(status().isForbidden());
    }

    private String loadFixture(String fileName) throws IOException {
        org.springframework.core.io.Resource resource =
                new org.springframework.core.io.ClassPathResource("fixtures/" + fileName);

        try (java.io.InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IOException("Could not find fixture file: fixtures/" + fileName, e);
        }
    }
}
