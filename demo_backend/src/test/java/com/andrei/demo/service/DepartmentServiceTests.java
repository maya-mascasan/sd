package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Department;
import com.andrei.demo.model.DepartmentCreateDTO;
import com.andrei.demo.repository.CourseRepository;
import com.andrei.demo.repository.DepartmentRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DepartmentServiceTests {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private DepartmentService departmentService;

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
    void testAddDepartment_Success() throws ValidationException {
        // given:
        DepartmentCreateDTO dto = new DepartmentCreateDTO();
        dto.setName("Automation");

        when(departmentRepository.findByName("Automation")).thenReturn(Optional.empty());
        // Updated to use the dynamic answer pattern from your teacher's code
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when:
        Department result = departmentService.addDepartment(dto);

        // then:
        assertEquals("Automation", result.getName());
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void testAddDepartment_DuplicateName() {
        // given:
        DepartmentCreateDTO dto = new DepartmentCreateDTO();
        dto.setName("Math");

        // when:
        when(departmentRepository.findByName("Math")).thenReturn(Optional.of(new Department()));

        // then:
        assertThrows(ValidationException.class, () -> departmentService.addDepartment(dto));
        verify(departmentRepository, never()).save(any());
    }

    @Test
    void testGetDepartments() {
        // given:
        List<Department> departments = List.of(new Department(), new Department());

        // when:
        when(departmentRepository.findAll()).thenReturn(departments);
        List<Department> result = departmentService.getDepartments();

        // then:
        assertEquals(2, result.size());
        verify(departmentRepository, times(1)).findAll();
    }

    @Test
    void testGetDepartmentById_Success() {
        // given:
        UUID id = UUID.randomUUID();
        Department department = new Department();
        department.setId(id);

        // when:
        when(departmentRepository.findById(id)).thenReturn(Optional.of(department));
        Department result = departmentService.getDepartmentById(id);

        // then:
        assertEquals(id, result.getId());
        verify(departmentRepository, times(1)).findById(id);
    }

    @Test
    void testGetDepartmentById_NotFound() {
        // given:
        UUID id = UUID.randomUUID();

        // when:
        when(departmentRepository.findById(id)).thenReturn(Optional.empty());

        // then:
        assertThrows(IllegalStateException.class, () -> departmentService.getDepartmentById(id));
    }

    @Test
    void testUpdateDepartment_Success() throws ValidationException {
        // given:
        UUID id = UUID.randomUUID();
        Department existing = new Department();
        existing.setId(id);
        existing.setName("Old Name");

        Department newDetails = new Department();
        newDetails.setName("New Name");

        when(departmentRepository.findById(id)).thenReturn(Optional.of(existing));
        when(departmentRepository.findByName("New Name")).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when:
        Department result = departmentService.updateDepartment(id, newDetails);

        // then:
        assertEquals("New Name", result.getName());
        verify(departmentRepository, times(1)).save(existing);
    }

    @Test
    void testPatchDepartment_NameAlreadyTaken() {
        // given:
        UUID id = UUID.randomUUID();
        Department existing = new Department();
        existing.setId(id);
        existing.setName("Physics");

        Map<String, Object> updates = Map.of("name", "Math");

        // when:
        when(departmentRepository.findById(id)).thenReturn(Optional.of(existing));
        when(departmentRepository.findByName("Math")).thenReturn(Optional.of(new Department()));

        // then:
        assertThrows(ValidationException.class, () -> departmentService.patchDepartment(id, updates));
    }

    @Test
    void testDeleteDepartment() throws ValidationException {
        // given:
        UUID id = UUID.randomUUID();

        // when:
        when(courseRepository.existsByDepartmentId(id)).thenReturn(false);
        doNothing().when(departmentRepository).deleteById(id);

        departmentService.deleteDepartment(id);

        // then:
        verify(courseRepository, times(1)).existsByDepartmentId(id);
        verify(departmentRepository, times(1)).deleteById(id);
    }

    @Test
    void testDeleteDepartment_ThrowsExceptionWhenCoursesExist() {
        // given:
        UUID id = UUID.randomUUID();

        // when:
        when(courseRepository.existsByDepartmentId(id)).thenReturn(true);

        // then:
        assertThrows(ValidationException.class, () -> {
            departmentService.deleteDepartment(id);
        });

        verify(departmentRepository, never()).deleteById(id);
    }
}