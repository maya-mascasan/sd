package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Submission;
import com.andrei.demo.model.SubmissionCreateDTO;
import com.andrei.demo.service.SubmissionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@AllArgsConstructor
@CrossOrigin
@RequestMapping("/submission")
public class SubmissionController {
    private final SubmissionService submissionService;

    @GetMapping("/assignment/{assignmentId}")
    public List<Submission> getByAssignment(@PathVariable UUID assignmentId) {
        return submissionService.getByAssignment(assignmentId);
    }

    @GetMapping("/student/{studentId}")
    public List<Submission> getByStudent(@PathVariable UUID studentId) {
        return submissionService.getByStudent(studentId);
    }

    @PostMapping
    public Submission submit(@Valid @RequestBody SubmissionCreateDTO dto) throws ValidationException {
        return submissionService.submit(dto);
    }

    @PatchMapping("/{id}/grade")
    public Submission grade(@PathVariable UUID id, @RequestBody Map<String, Double> body)
            throws ValidationException {
        return submissionService.grade(id, body.get("grade"));
    }
}