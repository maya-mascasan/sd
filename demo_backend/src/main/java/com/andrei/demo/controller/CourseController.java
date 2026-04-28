package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Course;
import com.andrei.demo.model.CourseCreateDTO;
import com.andrei.demo.service.CourseService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@AllArgsConstructor
@CrossOrigin
@RequestMapping("/course")
public class CourseController {
    private final CourseService courseService;

    @GetMapping
    public List<Course> getCourses() {
        return courseService.getCourses();
    }

    @GetMapping("/{id}")
    public Course getCourseById(@PathVariable UUID id) {
        return courseService.getCourseById(id);
    }

    @PostMapping
    public Course addCourse(@Valid @RequestBody CourseCreateDTO dto) throws ValidationException {
        return courseService.addCourse(dto);
    }

    @PutMapping("/{id}")
    public Course updateCourse(@PathVariable UUID id, @Valid @RequestBody CourseCreateDTO dto) throws ValidationException {
        return courseService.updateCourse(id, dto);
    }

    @PatchMapping("/{id}")
    public Course patchCourse(@PathVariable UUID id, @RequestBody Map<String, Object> updates) throws ValidationException {
        return courseService.patchCourse(id, updates);
    }

    @DeleteMapping("/{id}")
    public void deleteCourse(@PathVariable UUID id) {
        courseService.deleteCourse(id);
    }

    @GetMapping("/professor/{professorId}")
    public List<Course> getCoursesByProfessor(@PathVariable UUID professorId) {
        return courseService.getCoursesByProfessor(professorId);
    }
}