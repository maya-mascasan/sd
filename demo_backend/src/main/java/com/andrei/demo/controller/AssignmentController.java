package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Assignment;
import com.andrei.demo.model.AssignmentCreateDTO;
import com.andrei.demo.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@CrossOrigin
@RequestMapping("/assignment")
public class AssignmentController {
    private final AssignmentService assignmentService;

    @GetMapping("/course/{courseId}")
    public List<Assignment> getByCourse(@PathVariable UUID courseId) {
        return assignmentService.getByCourse(courseId);
    }

    @PostMapping
    public Assignment create(@Valid @RequestBody AssignmentCreateDTO dto) throws ValidationException {
        return assignmentService.create(dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        assignmentService.delete(id);
    }
}