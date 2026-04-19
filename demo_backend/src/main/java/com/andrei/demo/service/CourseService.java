package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Course;
import com.andrei.demo.model.CourseCreateDTO;
import com.andrei.demo.model.Department;
import com.andrei.demo.repository.CourseRepository;
import com.andrei.demo.repository.DepartmentRepository;
import com.andrei.demo.repository.PersonRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;

    public List<Course> getCourses() {
        return courseRepository.findAll();
    }

    public Course getCourseById(UUID id) {
        return courseRepository.findById(id).orElseThrow(
                () -> new IllegalStateException("Course with id " + id + " not found"));
    }

    public Course addCourse(CourseCreateDTO dto) throws ValidationException {
        //1:m validation
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ValidationException("Cannot create course: Department with ID " + dto.getDepartmentId() + " does not exist."));

        Course course = new Course();
        course.setTitle(dto.getTitle());
        course.setCredits(dto.getCredits());
        course.setDepartment(department);

        return courseRepository.save(course);
    }

    public Course updateCourse(UUID id, CourseCreateDTO dto) throws ValidationException {
        Course existingCourse = getCourseById(id);

        //1:m validation
        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ValidationException("Cannot update course: Department with ID " + dto.getDepartmentId() + " does not exist."));

        existingCourse.setTitle(dto.getTitle());
        existingCourse.setCredits(dto.getCredits());
        existingCourse.setDepartment(department);

        return courseRepository.save(existingCourse);
    }

    public Course patchCourse(UUID id, Map<String, Object> updates) throws ValidationException {
        Course existingCourse = getCourseById(id);

        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            switch (key) {
                case "title":
                    existingCourse.setTitle((String) value);
                    break;
                case "credits":
                    existingCourse.setCredits((Integer) value);
                    break;
                case "departmentId":
                    UUID deptId = UUID.fromString((String) value);
                    Department department = departmentRepository.findById(deptId) //1:m validation
                            .orElseThrow(() -> new ValidationException("Cannot patch course: Department with ID " + deptId + " does not exist."));
                    existingCourse.setDepartment(department);
                    break;
            }
        }

        return courseRepository.save(existingCourse);
    }

    public void deleteCourse(UUID id) {
        courseRepository.deleteById(id);
    }
}