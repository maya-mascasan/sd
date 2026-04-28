package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Assignment;
import com.andrei.demo.model.AssignmentCreateDTO;
import com.andrei.demo.model.Course;
import com.andrei.demo.repository.AssignmentRepository;
import com.andrei.demo.repository.CourseRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AssignmentService {
    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;

    public List<Assignment> getByCourse(UUID courseId) {
        return assignmentRepository.findByCourseId(courseId);
    }

    public Assignment create(AssignmentCreateDTO dto) throws ValidationException {
        Course course = courseRepository.findById(dto.getCourseId())
                .orElseThrow(() -> new ValidationException("Course not found: " + dto.getCourseId()));
        Assignment a = new Assignment();
        a.setTitle(dto.getTitle());
        a.setDescription(dto.getDescription());
        a.setDeadline(dto.getDeadline());
        a.setCourse(course);
        return assignmentRepository.save(a);
    }

    public void delete(UUID id) {
        assignmentRepository.deleteById(id);
    }
}