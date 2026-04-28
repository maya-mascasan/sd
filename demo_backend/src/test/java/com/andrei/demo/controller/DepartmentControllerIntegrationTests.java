package com.andrei.demo.controller;

import com.andrei.demo.config.JwtService;
import com.andrei.demo.model.Department;
import com.andrei.demo.repository.DepartmentRepository;
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

    @Autowired
    private JwtService jwtService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    // JWT token for an admin user — used in all requests
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        departmentRepository.deleteAll();
        departmentRepository.flush();
        seedDatabase();

        adminToken = jwtService.generateToken("test-admin@example.com", "admin");
    }

    private void seedDatabase() throws Exception {
        String seedDataJson = loadFixture("department_seed.json");
        List<Department> depts = objectMapper.readValue(seedDataJson, new TypeReference<>() {});
        departmentRepository.saveAll(depts);
    }

    @Test
    void testGetDepartments() throws Exception {
        mockMvc.perform(get("/department")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].name",
                        Matchers.containsInAnyOrder("Mathematics", "Computer Science")));
    }

    @Test
    void testAddDepartment_ValidPayload() throws Exception {
        String validDeptJson = loadFixture("valid_department.json");

        mockMvc.perform(post("/department")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validDeptJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Automation and Computing"));
    }

    @Test
    void testAddDepartment_InvalidPayload_DuplicateName() throws Exception {
        // "Mathematics" already exists in the seed data — adding it again
        // triggers the duplicate-name validation in DepartmentService,
        // which throws ValidationException -> 400 Bad Request.
        String invalidDeptJson = loadFixture("invalid_department.json");

        mockMvc.perform(post("/department")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidDeptJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetDepartments_WithoutToken_Unauthorized() throws Exception {
        mockMvc.perform(get("/department"))
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
