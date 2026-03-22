package com.andrei.demo.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class CourseCreateDTO {

    @NotBlank(message = "Course title is required")
    private String title;

    @NotNull(message = "Credits are required")
    @Min(value = 1, message = "Course needs to have at least 1 credit")
    @Max(value = 30, message = "Course can have at most 30 credits")
    private Integer credits;

    @NotNull(message = "Department ID is required")
    private UUID departmentId;
}