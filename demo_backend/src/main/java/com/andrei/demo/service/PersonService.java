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

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PersonService {
    private final PersonRepository personRepository;
    private final CourseRepository courseRepository;
    private final PasswordUtil passwordUtil;

    public List<Person> getPeople() {
        return personRepository.findAll();
    }

    public Person addPerson(PersonCreateDTO personDTO) throws ValidationException {
        // duplicate email
        if (personRepository.findByEmail(personDTO.getEmail()).isPresent()) {
            throw new ValidationException("A user with the email " + personDTO.getEmail() + " already exists.");
        }

        Person person = new Person();
        person.setName(personDTO.getName());
        person.setAge(personDTO.getAge());
        person.setEmail(personDTO.getEmail());
        String hashedPassword = passwordUtil.hashPassword(personDTO.getPassword());
        person.setPassword(hashedPassword);

        if (personDTO.getRole() != null) {
            person.setRole(personDTO.getRole());
        } else {
            person.setRole(Role.admin);
        }

        if (personDTO.getCourseIds() != null && !personDTO.getCourseIds().isEmpty()) {
            List<Course> coursesToLink = courseRepository.findAllById(personDTO.getCourseIds());
            person.getEnrolledCourses().addAll(coursesToLink);
        }

        return personRepository.save(person);
    }

    public Person updatePerson(UUID uuid, Person person) throws ValidationException {
        Person existingPerson = personRepository.findById(uuid)
                .orElseThrow(() -> new ValidationException("Person with id " + uuid + " not found"));

        if (!existingPerson.getEmail().equals(person.getEmail()) &&
                personRepository.findByEmail(person.getEmail()).isPresent()) {
            throw new ValidationException("The email " + person.getEmail() + " is already taken by another user.");
        }

        existingPerson.setName(person.getName());
        existingPerson.setAge(person.getAge());
        existingPerson.setEmail(person.getEmail());
        String hashedPassword = passwordUtil.hashPassword(person.getPassword());
        existingPerson.setPassword(hashedPassword);
        if (person.getRole() != null) {
            existingPerson.setRole(person.getRole());
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
}