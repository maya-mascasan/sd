package com.andrei.demo.service;

import com.andrei.demo.config.ValidationException;
import com.andrei.demo.model.PasswordResetToken;
import com.andrei.demo.model.Person;
import com.andrei.demo.repository.PasswordResetTokenRepository;
import com.andrei.demo.repository.PersonRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@AllArgsConstructor
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final PersonRepository personRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void requestReset(String email) throws ValidationException {
        personRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("No account found with email: " + email));

        // MANUALLY delete if exists
        tokenRepository.findByEmail(email).ifPresent(tokenRepository::delete);

        // Crucial: Flush the deletion to the DB before inserting the new one
        tokenRepository.flush();

        String code = String.format("%06d", new Random().nextInt(999999));

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(email);
        token.setCode(code);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        tokenRepository.save(token);

        emailService.sendResetCode(email, code);
    }

    @Transactional
    public void confirmReset(String email, String code, String newPassword) throws ValidationException {
        PasswordResetToken token = tokenRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("No reset request found for this email."));

        if (token.isExpired()) {
            tokenRepository.delete(token);
            throw new ValidationException("Reset code has expired. Please request a new one.");
        }

        if (!token.getCode().equals(code)) {
            throw new ValidationException("Invalid reset code.");
        }

        Person person = personRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("User not found."));
        person.setPassword(passwordEncoder.encode(newPassword));
        personRepository.save(person);

        tokenRepository.delete(token);

        // Send confirmation notification
        emailService.sendPasswordChangedConfirmation(email);
    }
}