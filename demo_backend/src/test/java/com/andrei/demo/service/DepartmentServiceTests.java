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
        DepartmentCreateDTO dto = new DepartmentCreateDTO();
        dto.setName("Automation");

        Department savedDept = new Department();
        savedDept.setName("Automation");

        when(departmentRepository.findByName("Automation")).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenReturn(savedDept);
        Department result = departmentService.addDepartment(dto);

        assertEquals("Automation", result.getName());
        verify(departmentRepository, times(1)).save(any(Department.class));
    }

    @Test
    void testAddDepartment_DuplicateName() {
        DepartmentCreateDTO dto = new DepartmentCreateDTO();
        dto.setName("Math");

        when(departmentRepository.findByName("Math")).thenReturn(Optional.of(new Department()));

        assertThrows(ValidationException.class, () -> departmentService.addDepartment(dto));
        verify(departmentRepository, never()).save(any());
    }

    @Test
    void testGetDepartments() {
        List<Department> departments = List.of(new Department(), new Department());

        when(departmentRepository.findAll()).thenReturn(departments);
        List<Department> result = departmentService.getDepartments();

        assertEquals(2, result.size());
        verify(departmentRepository, times(1)).findAll();
    }

    @Test
    void testGetDepartmentById_Success() {
        UUID id = UUID.randomUUID();
        Department department = new Department();
        department.setId(id);

        when(departmentRepository.findById(id)).thenReturn(Optional.of(department));
        Department result = departmentService.getDepartmentById(id);

        assertEquals(id, result.getId());
        verify(departmentRepository, times(1)).findById(id);
    }

    @Test
    void testGetDepartmentById_NotFound() {
        UUID id = UUID.randomUUID();
        when(departmentRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(IllegalStateException.class, () -> departmentService.getDepartmentById(id));
    }

    @Test
    void testUpdateDepartment_Success() throws ValidationException {
        UUID id = UUID.randomUUID();
        Department existing = new Department();
        existing.setId(id);
        existing.setName("Old Name");

        Department newDetails = new Department();
        newDetails.setName("New Name");

        when(departmentRepository.findById(id)).thenReturn(Optional.of(existing));
        when(departmentRepository.findByName("New Name")).thenReturn(Optional.empty());
        when(departmentRepository.save(any(Department.class))).thenAnswer(i -> i.getArguments()[0]);

        Department result = departmentService.updateDepartment(id, newDetails);

        assertEquals("New Name", result.getName());
        verify(departmentRepository).save(existing);
    }

    @Test
    void testPatchDepartment_NameAlreadyTaken() {
        UUID id = UUID.randomUUID();
        Department existing = new Department();
        existing.setId(id);
        existing.setName("Physics");

        Map<String, Object> updates = Map.of("name", "Math");

        when(departmentRepository.findById(id)).thenReturn(Optional.of(existing));
        when(departmentRepository.findByName("Math")).thenReturn(Optional.of(new Department()));

        assertThrows(ValidationException.class, () -> departmentService.patchDepartment(id, updates));
    }

    @Test
    void testDeleteDepartment() throws ValidationException {
        UUID id = UUID.randomUUID();

        when(courseRepository.existsByDepartmentId(id)).thenReturn(false);

        doNothing().when(departmentRepository).deleteById(id);
        departmentService.deleteDepartment(id);

        verify(courseRepository, times(1)).existsByDepartmentId(id);
        verify(departmentRepository, times(1)).deleteById(id);
    }

    @Test
    void testDeleteDepartment_ThrowsExceptionWhenCoursesExist() {
        UUID id = UUID.randomUUID();

        when(courseRepository.existsByDepartmentId(id)).thenReturn(true);

        assertThrows(ValidationException.class, () -> {
            departmentService.deleteDepartment(id);
        });

        verify(departmentRepository, never()).deleteById(id);
    }
}