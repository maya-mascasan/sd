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
        List<Course> courses = List.of(new Course(), new Course());

        when(courseRepository.findAll()).thenReturn(courses);
        List<Course> result = courseService.getCourses();

        assertEquals(2, result.size());
        verify(courseRepository, times(1)).findAll();
    }

    @Test
    void testAddCourse_Success() throws ValidationException {
        UUID deptId = UUID.randomUUID();
        CourseCreateDTO dto = new CourseCreateDTO();
        dto.setTitle("Software Design");
        dto.setCredits(5);
        dto.setDepartmentId(deptId);

        Department dept = new Department();
        dept.setId(deptId);

        Course savedCourse = new Course();
        savedCourse.setTitle("Software Design");
        savedCourse.setDepartment(dept);

        when(departmentRepository.findById(deptId)).thenReturn(Optional.of(dept));
        when(courseRepository.save(any(Course.class))).thenReturn(savedCourse);
        Course result = courseService.addCourse(dto);

        assertNotNull(result);
        assertEquals("Software Design", result.getTitle());
        verify(departmentRepository, times(1)).findById(deptId);
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void testAddCourse_DepartmentNotFound() {
        UUID deptId = UUID.randomUUID();
        CourseCreateDTO dto = new CourseCreateDTO();
        dto.setDepartmentId(deptId);

        when(departmentRepository.findById(deptId)).thenReturn(Optional.empty());

        assertThrows(ValidationException.class, () -> courseService.addCourse(dto));
    }

    @Test
    void testUpdateCourse_Success() throws ValidationException {
        UUID courseId = UUID.randomUUID();
        UUID deptId = UUID.randomUUID();

        CourseCreateDTO dto = new CourseCreateDTO();
        dto.setTitle("Advanced SD");
        dto.setCredits(6);
        dto.setDepartmentId(deptId);

        Course existingCourse = new Course();
        existingCourse.setId(courseId);
        existingCourse.setTitle("Basic SD");

        Department dept = new Department();
        dept.setId(deptId);

        Course updatedCourse = new Course();
        updatedCourse.setId(courseId);
        updatedCourse.setTitle("Advanced SD");
        updatedCourse.setCredits(6);
        updatedCourse.setDepartment(dept);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(existingCourse));
        when(departmentRepository.findById(deptId)).thenReturn(Optional.of(dept));
        when(courseRepository.save(any(Course.class))).thenReturn(updatedCourse);

        Course result = courseService.updateCourse(courseId, dto);

        assertEquals("Advanced SD", result.getTitle());
        assertEquals(6, result.getCredits());
        verify(courseRepository, times(1)).findById(courseId);
        verify(courseRepository, times(1)).save(any(Course.class));
    }

    @Test
    void testUpdateCourse_CourseNotFound() {
        UUID courseId = UUID.randomUUID();
        CourseCreateDTO dto = new CourseCreateDTO();

        when(courseRepository.findById(courseId)).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> courseService.updateCourse(courseId, dto));
    }
    @Test
    void testPatchCourse_UpdateCredits() throws ValidationException {
        UUID courseId = UUID.randomUUID();
        Course existing = new Course();
        existing.setId(courseId);
        existing.setCredits(2);

        Map<String, Object> updates = new HashMap<>();
        updates.put("credits", 6);

        when(courseRepository.findById(courseId)).thenReturn(Optional.of(existing));
        when(courseRepository.save(any(Course.class))).thenAnswer(i -> i.getArguments()[0]);
        Course result = courseService.patchCourse(courseId, updates);

        assertEquals(6, result.getCredits());
        verify(courseRepository, times(1)).save(existing);
    }
    @Test
    void testDeleteCourse() {
        UUID uuid = UUID.randomUUID();

        doNothing().when(courseRepository).deleteById(uuid);
        courseService.deleteCourse(uuid);

        verify(courseRepository, times(1)).deleteById(uuid);
    }
}