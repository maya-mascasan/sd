package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Course;
import com.andrei.demo.model.CourseCreateDTO;
import com.andrei.demo.model.Department;
import com.andrei.demo.repository.CourseRepository;
import com.andrei.demo.repository.DepartmentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CourseServiceTests {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private CourseService courseService;

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
    void testGetCourses() {
        // given:
        List<Course> courses = List.of(new Course(), new Course());

        // when:
        when(courseRepository.findAll()).thenReturn(courses);
        List<Course> result = courseService.getCourses();

        // then:
        assertEquals(2, result.size());
        verify(courseRepository, times(1)).findAll();
    }

    @Test
    void testAddCourse_Success() throws ValidationException {
        // given:
        UUID deptId = UUID.randomUUID();
        CourseCreateDTO dto = new CourseCreateDTO();
        dto.setTitle("Software Design");
        dto.setCredits(5);
        dto.setDepartmentId(deptId);

        Department dept = new Department();
        dept.setId(deptId);

        when(departmentRepository.findById(deptId)).thenReturn(Optional.of(dept));
        // Updated to use the dynamic answer pattern
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when:
        Course result = courseService.addCourse(dto);

        // then:
        assertNotNull(result);
        assertEquals("Software Design", result.getTitle());
        assertEquals(5, result.getCredits());
        assertEquals(dept, result.getDepartment());
        verify(departmentRepository, times(1)).findById(deptId);
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void testAddCourse_DepartmentNotFound() {
        // given:
        UUID deptId = UUID.randomUUID();
        CourseCreateDTO dto = new CourseCreateDTO();
        dto.setDepartmentId(deptId);

        // when:
        when(departmentRepository.findById(deptId)).thenReturn(Optional.empty());

        // then:
        assertThrows(ValidationException.class, () -> courseService.addCourse(dto));
    }

    @Test
    void testUpdateCourse_Success() throws ValidationException {
        // given:
        UUID courseId = UUID.randomUUID();
        UUID deptId = UUID.randomUUID();

        CourseCreateDTO dto = new CourseCreateDTO();
        dto.setTitle("Advanced SD");
        dto.setCredits(6);
        dto.setDepartmentId(deptId);

        Course existingCourse = new Course();
        existingCourse.setId(courseId);
        existingCourse.setTitle("Basic SD");
        existingCourse.setCredits(3);

        Department dept = new Department();
        dept.setId(deptId);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(existingCourse));
        when(departmentRepository.findById(deptId)).thenReturn(Optional.of(dept));
        // Updated to use the dynamic answer pattern
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when:
        Course result = courseService.updateCourse(courseId, dto);

        // then:
        assertEquals("Advanced SD", result.getTitle());
        assertEquals(6, result.getCredits());
        assertEquals(dept, result.getDepartment());
        verify(courseRepository, times(1)).findById(courseId);
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void testUpdateCourse_CourseNotFound() {
        // given:
        UUID courseId = UUID.randomUUID();
        CourseCreateDTO dto = new CourseCreateDTO();

        // when:
        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        // then:
        assertThrows(IllegalStateException.class, () -> courseService.updateCourse(courseId, dto));
    }

    @Test
    void testPatchCourse_UpdateCredits() throws ValidationException {
        // given:
        UUID courseId = UUID.randomUUID();
        Course existing = new Course();
        existing.setId(courseId);
        existing.setCredits(2);

        Map<String, Object> updates = new HashMap<>();
        updates.put("credits", 6);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(existing));
        // Standardized to use the exact same dynamic answer pattern
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when:
        Course result = courseService.patchCourse(courseId, updates);

        // then:
        assertEquals(6, result.getCredits());
        verify(courseRepository, times(1)).save(existing);
    }

    @Test
    void testDeleteCourse() {
        // given:
        UUID uuid = UUID.randomUUID();

        // when:
        doNothing().when(courseRepository).deleteById(uuid);
        courseService.deleteCourse(uuid);

        // then:
        verify(courseRepository, times(1)).deleteById(uuid);
    }
}