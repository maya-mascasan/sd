package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.Course;
import com.andrei.demo.model.Person;
import com.andrei.demo.model.PersonCreateDTO;
import com.andrei.demo.model.Role;
import com.andrei.demo.repository.CourseRepository;
import com.andrei.demo.repository.PersonRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.andrei.demo.util.PasswordUtil;
import org.springframework.transaction.annotation.Transactional; // Add import

import java.util.*;

@Service
@AllArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;
    private final CourseRepository courseRepository;
    private final PasswordUtil passwordUtil;

    public List<Person> getPeople() {
        return personRepository.findAll();
    }

    @Transactional // Make sure this is added to the method!
    public Person addPerson(PersonCreateDTO personDTO) throws ValidationException {
        if (personRepository.findByEmail(personDTO.getEmail()).isPresent()) {
            throw new ValidationException("A user with the email " + personDTO.getEmail() + " already exists.");
        }

        Person person = new Person();
        person.setName(personDTO.getName());
        person.setAge(personDTO.getAge());
        person.setEmail(personDTO.getEmail());
        person.setPassword(passwordUtil.hashPassword(personDTO.getPassword()));
        person.setRole(personDTO.getRole() != null ? personDTO.getRole() : Role.admin);

        // 1. SAVE THE PERSON FIRST to generate the ID
        person = personRepository.save(person);

        // 2. NOW handle the courses
        if (personDTO.getCourses() != null && !personDTO.getCourses().isEmpty()) {
            List<UUID> ids = personDTO.getCourses().stream()
                    .map(Course::getId)
                    .toList();

            List<Course> managedCourses = courseRepository.findAllById(ids);

            if (person.getRole() == Role.professor) {
                for (Course c : managedCourses) {
                    // Now 'person' is a persistent entity with an ID
                    c.setProfessor(person);
                    courseRepository.save(c);
                }
            }

            person.getEnrolledCourses().addAll(managedCourses);
        }

        // 3. Final save to update the relationships
        return personRepository.save(person);
    }

    @Transactional
    public Person updatePerson(UUID uuid, Person person) throws ValidationException {
        Person existingPerson = personRepository.findById(uuid)
                .orElseThrow(() -> new ValidationException("Person not found"));

        // 1. Basic fields
        existingPerson.setName(person.getName());
        existingPerson.setAge(person.getAge());
        existingPerson.setEmail(person.getEmail());
        existingPerson.setRole(person.getRole());

        // 2. Password logic
        if (person.getPassword() != null && !person.getPassword().isBlank()) {
            existingPerson.setPassword(passwordUtil.hashPassword(person.getPassword()));
        }

        // 3. Collection Syncing (One single block)
        existingPerson.getEnrolledCourses().clear();

        if (person.getEnrolledCourses() != null && !person.getEnrolledCourses().isEmpty()) {
            List<UUID> ids = person.getEnrolledCourses().stream()
                    .map(Course::getId)
                    .filter(Objects::nonNull)
                    .toList();

            List<Course> managed = courseRepository.findAllById(ids);

            // If the person is a professor, update the Course table ownership
            if (existingPerson.getRole() == Role.professor) {
                for (Course c : managed) {
                    c.setProfessor(existingPerson);
                    // We save the course to update the professor_id column
                    courseRepository.save(c);
                }
            }

            existingPerson.getEnrolledCourses().addAll(managed);
        }

        return personRepository.save(existingPerson);
    }

    public Person patchPerson(UUID uuid, Map<String, Object> updates) throws ValidationException {
        Person existingPerson = personRepository.findById(uuid).orElseThrow(() -> new ValidationException("Person with id " + uuid + " not found"));

        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            switch (key) {
                case "name":
                    existingPerson.setName((String) value);
                    break;
                case "age":
                    existingPerson.setAge((Integer) value);
                    break;
                case "email":
                    String newEmail = (String) value;
                    if (!existingPerson.getEmail().equals(newEmail) &&
                            personRepository.findByEmail(newEmail).isPresent()) {
                        throw new ValidationException("The email " + newEmail + " is already taken.");
                    }
                    existingPerson.setEmail(newEmail);
                    break;
                case "password":
                    existingPerson.setPassword((String) value);
                    break;
                case "role":
                    existingPerson.setRole(Role.valueOf(value.toString().toLowerCase()));
                    break;
            }
        }

        return personRepository.save(existingPerson);
    }

    public void deletePerson(UUID uuid) {
        personRepository.deleteById(uuid);
    }

    public Person getPersonByEmail(String email) {
        return personRepository.findByEmail(email).orElseThrow(
                () -> new IllegalStateException("Person with email " + email + " not found"));
    }

    public Person getPersonById(UUID uuid) {
        return personRepository.findById(uuid).orElseThrow(
                () -> new IllegalStateException("Person with id " + uuid + " not found"));
    }

    @Transactional // Add this!
    public Person enrollCourse(UUID personId, UUID courseId) throws ValidationException {
        Person person = getPersonById(personId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ValidationException("Course not found."));

        // Add the course to the person
        if (!person.getEnrolledCourses().contains(course)) {
            person.getEnrolledCourses().add(course);
        }

        // Save and return
        return personRepository.save(person);
    }

    @Transactional // Add this too!
    public Person unenrollCourse(UUID personId, UUID courseId) throws ValidationException {
        Person person = getPersonById(personId);
        person.getEnrolledCourses().removeIf(c -> c.getId().equals(courseId));
        return personRepository.save(person);
    }
}