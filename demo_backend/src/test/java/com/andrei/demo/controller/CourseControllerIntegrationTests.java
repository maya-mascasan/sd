package com.andrei.demo.controller;

import com.andrei.demo.config.JwtService;
import com.andrei.demo.model.Course;
import com.andrei.demo.model.Department;
import com.andrei.demo.repository.CourseRepository;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
public class CourseControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private JwtService jwtService;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private UUID existingDeptId;

    // JWT token for an admin user — used in all requests
    private String adminToken;

    @BeforeEach
    void setUp() throws Exception {
        courseRepository.deleteAll();
        departmentRepository.deleteAll();
        courseRepository.flush();
        departmentRepository.flush();

        Department dept = new Department();
        dept.setName("Computer Science");
        existingDeptId = departmentRepository.save(dept).getId();

        seedDatabase();

        adminToken = jwtService.generateToken("test-admin@example.com", "admin");
    }

    private void seedDatabase() throws Exception {
        String seedDataJson = loadFixture("course_seed.json");
        List<Course> courses = objectMapper.readValue(seedDataJson, new TypeReference<>() {});

        courses.forEach(c -> c.setDepartment(departmentRepository.findById(existingDeptId).get()));

        courseRepository.saveAll(courses);
    }

    @Test
    void testGetCourses() throws Exception {
        mockMvc.perform(get("/course")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].title",
                        Matchers.containsInAnyOrder("Calculus I", "Data Structures")));
    }

    @Test
    void testAddCourse_ValidPayload() throws Exception {
        String rawJson = loadFixture("valid_course.json");
        String processedJson = rawJson.replace("id", existingDeptId.toString());

        mockMvc.perform(post("/course")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(processedJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Software Design"))
                .andExpect(jsonPath("$.department.id").value(existingDeptId.toString()));
    }

    @Test
    void testAddCourse_InvalidPayload() throws Exception {
        String invalidCourseJson = loadFixture("invalid_course.json");

        mockMvc.perform(post("/course")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidCourseJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Course title is required"))
                .andExpect(jsonPath("$.credits").value("Course needs to have at least 1 credit"))
                .andExpect(jsonPath("$.departmentId").value("Department ID is required"));
    }

    @Test
    void testGetCourses_WithoutToken_Unauthorized() throws Exception {
        mockMvc.perform(get("/course"))
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
