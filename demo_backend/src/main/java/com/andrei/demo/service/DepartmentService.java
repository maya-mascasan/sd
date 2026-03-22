package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Department;
import com.andrei.demo.model.DepartmentCreateDTO;
import com.andrei.demo.repository.DepartmentRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public List<Department> getDepartments() {
        return departmentRepository.findAll();
    }

    public Department getDepartmentById(UUID id) {
        return departmentRepository.findById(id).orElseThrow(
                () -> new IllegalStateException("Department with id " + id + " not found"));
    }

    public Department addDepartment(DepartmentCreateDTO dto) throws ValidationException {
        if (departmentRepository.findByName(dto.getName()).isPresent()) {
            throw new ValidationException("A department named '" + dto.getName() + "' already exists.");
        }

        Department department = new Department();
        department.setName(dto.getName());

        return departmentRepository.save(department);
    }

    public Department updateDepartment(UUID id, Department departmentDetails) throws ValidationException {
        Department existing = getDepartmentById(id);

        if (!existing.getName().equals(departmentDetails.getName()) &&
                departmentRepository.findByName(departmentDetails.getName()).isPresent()) {
            throw new ValidationException("The name '" + departmentDetails.getName() + "' is already taken by another department.");
        }

        existing.setName(departmentDetails.getName());
        return departmentRepository.save(existing);
    }

    public Department patchDepartment(UUID id, Map<String, Object> updates) throws ValidationException {
        Department existing = getDepartmentById(id);

        if (updates.containsKey("name")) {
            String newName = (String) updates.get("name");
            if (!existing.getName().equals(newName) &&
                    departmentRepository.findByName(newName).isPresent()) {
                throw new ValidationException("The name '" + newName + "' is already taken.");
            }
            existing.setName(newName);
        }

        return departmentRepository.save(existing);
    }

    public void deleteDepartment(UUID id) {
        departmentRepository.deleteById(id);
    }
}