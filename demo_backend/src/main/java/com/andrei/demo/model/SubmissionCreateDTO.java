package com.andrei.demo.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SubmissionCreateDTO {

    @NotBlank(message = "Submission content cannot be empty")
    private String content;

    @NotNull(message = "Assignment ID is required")
    private UUID assignmentId;

    @NotNull(message = "Student ID is required")
    private UUID studentId;
}