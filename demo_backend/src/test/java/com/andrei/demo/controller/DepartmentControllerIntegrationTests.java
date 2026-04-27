package com.andrei.demo.controller;

import com.andrei.demo.model.Department;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.Role;
import com.andrei.demo.repository.DepartmentRepository;
import com.andrei.demo.repository.PersonRepository;
import com.andrei.demo.util.JwtUtil;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class DepartmentControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DepartmentRepository departmentRepository;

    // Added PersonRepository to save a dummy user for the token
    @Autowired
    private PersonRepository personRepository;

    // Added JwtUtil to generate the token
    @Autowired
    private JwtUtil jwtUtil;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // Added authToken variable
    private String authToken;

    @BeforeEach
    void setUp() throws Exception {
        departmentRepository.deleteAll();
        departmentRepository.flush();

        // Clean person repo to prevent data collisions
        personRepository.deleteAll();
        personRepository.flush();

        seedDatabase();
        initializeAuthToken(); // Initialize the token before tests run
    }

    private void seedDatabase() throws Exception {
        String seedDataJson = loadFixture("department_seed.json");
        List<Department> depts = objectMapper.readValue(seedDataJson, new TypeReference<>() {});
        departmentRepository.saveAll(depts);
    }

    private void initializeAuthToken() {
        // Create and save a dummy person specifically to generate a valid JWT
        Person authPerson = new Person();
        authPerson.setName("Test Admin");
        authPerson.setEmail("admin@example.com");
        authPerson.setPassword("Securepass123!@#");
        authPerson.setAge(30);

        authPerson.setRole(com.andrei.demo.model.Role.admin); // Notice it's lowercase 'admin' based on your enum!
        authPerson = personRepository.save(authPerson);


        // Generate the token
        authToken = jwtUtil.createToken(authPerson);
    }

    @Test
    void testGetDepartments() throws Exception {
        mockMvc.perform(get("/department")
                        // Injected Authorization header
                        .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].name",
                        Matchers.containsInAnyOrder("Mathematics", "Computer Science")));
    }

    @Test
    void testAddDepartment_ValidPayload() throws Exception {
        String validDeptJson = loadFixture("valid_department.json");

        mockMvc.perform(post("/department")
                        // Injected Authorization header
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validDeptJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Automation and Computing"));
    }

    @Test
    void testAddDepartment_InvalidPayload() throws Exception {
        String invalidDeptJson = loadFixture("invalid_department.json");

        mockMvc.perform(post("/department")
                        // Injected Authorization header
                        .header("Authorization", "Bearer " + authToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidDeptJson))
                .andExpect(status().isBadRequest());
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