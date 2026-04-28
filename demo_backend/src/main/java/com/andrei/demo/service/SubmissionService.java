package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.*;
import com.andrei.demo.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final PersonRepository personRepository;

    public List<Submission> getByAssignment(UUID assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId);
    }

    public List<Submission> getByStudent(UUID studentId) {
        return submissionRepository.findByStudentId(studentId);
    }

    public Submission submit(SubmissionCreateDTO dto) throws ValidationException {
        Assignment assignment = assignmentRepository.findById(dto.getAssignmentId())
                .orElseThrow(() -> new ValidationException("Assignment not found."));
        Person student = personRepository.findById(dto.getStudentId())
                .orElseThrow(() -> new ValidationException("Student not found."));

        // One submission per student per assignment
        submissionRepository.findByAssignmentIdAndStudentId(dto.getAssignmentId(), dto.getStudentId())
                .ifPresent(s -> { throw new RuntimeException("Already submitted."); });

        Submission s = new Submission();
        s.setContent(dto.getContent());
        s.setAssignment(assignment);
        s.setStudent(student);
        s.setSubmissionDate(LocalDate.now());
        return submissionRepository.save(s);
    }

    public Submission grade(UUID submissionId, Double grade) throws ValidationException {
        Submission s = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ValidationException("Submission not found."));
        s.setGrade(grade);
        return submissionRepository.save(s);
    }
}