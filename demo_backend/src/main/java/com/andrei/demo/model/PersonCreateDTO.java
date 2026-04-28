package com.andrei.demo.model;

import com.andrei.demo.validator.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PersonCreateDTO {
    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name should be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Password is required")
    @StrongPassword(message = "Password must contain at least 8 characters, including uppercase, lowercase, digit, and special character")
    private String password;

    // Jackson will map the Angular [{id: '...'}] here
    private List<Course> courses;

    @NotNull(message = "Age is required")
    private Integer age;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    // We can remove courseIds since we are using 'courses' now
    // private List<UUID> courseIds; 

    private Role role;

    // Lombok's @Data already creates getCourses(), 
    // but if you want a custom one for the Service logic:
    public List<Course> getCourses() {
        return courses;
    }

}