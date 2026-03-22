package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Department;
import com.andrei.demo.model.DepartmentCreateDTO;
import com.andrei.demo.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@AllArgsConstructor
@CrossOrigin
@RequestMapping("/department")
public class DepartmentController {
    private final DepartmentService departmentService;

    @GetMapping
    public List<Department> getDepartments() {
        return departmentService.getDepartments();
    }

    @GetMapping("/{id}")
    public Department getDepartmentById(@PathVariable UUID id) {
        return departmentService.getDepartmentById(id);
    }

    @PostMapping
    public Department addDepartment(@Valid @RequestBody DepartmentCreateDTO dto) throws ValidationException {
        return departmentService.addDepartment(dto);
    }

    @PutMapping("/{id}")
    public Department updateDepartment(@PathVariable UUID id, @RequestBody Department department) throws ValidationException {
        return departmentService.updateDepartment(id, department);
    }

    @PatchMapping("/{id}")
    public Department patchDepartment(@PathVariable UUID id, @RequestBody Map<String, Object> updates) throws ValidationException {
        return departmentService.patchDepartment(id, updates);
    }

    @DeleteMapping("/{id}")
    public void deleteDepartment(@PathVariable UUID id) {
        departmentService.deleteDepartment(id);
    }
}