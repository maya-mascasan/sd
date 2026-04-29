package com.andrei.demo.controller;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.PasswordResetConfirmDTO;
import com.andrei.demo.model.PasswordResetRequestDTO;
import com.andrei.demo.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@AllArgsConstructor
@CrossOrigin
@RequestMapping("/password-reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @PostMapping("/request")
    public ResponseEntity<Map<String, String>> requestReset(@Valid @RequestBody PasswordResetRequestDTO dto)
            throws ValidationException {
        passwordResetService.requestReset(dto.getEmail());

        // Fix: Return a JSON Map instead of a raw String
        return ResponseEntity.ok(Map.of("message", "Reset code sent to your email."));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Map<String, String>> confirmReset(@Valid @RequestBody PasswordResetConfirmDTO dto)
            throws ValidationException {
        passwordResetService.confirmReset(dto.getEmail(), dto.getCode(), dto.getNewPassword());

        // Fix: Return a JSON Map instead of a raw String
        return ResponseEntity.ok(Map.of("message", "Password reset successfully."));
    }
}