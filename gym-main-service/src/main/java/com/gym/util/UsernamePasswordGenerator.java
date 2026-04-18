package com.gym.util;

import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Collection;

@Slf4j
@Component
public class UsernamePasswordGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 10;
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generateUsername(String firstName, String lastName, Collection<?> existing) {
        log.debug("Generating username for: {} {}", firstName, lastName);
        String base = firstName + "." + lastName;
        long count = existing.stream()
                .filter(e -> getUsernameFrom(e).startsWith(base))
                .count();
        return count == 0 ? base : base + count;
    }

    public String generatePassword() {
        log.debug("Generating random password");
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }

    private String getUsernameFrom(Object entity) {
        if (entity instanceof User user) {
            return user.getUsername() != null ? user.getUsername() : "";
        }
        if (entity instanceof Trainee trainee && trainee.getUser() != null) {
            return trainee.getUser().getUsername() != null
                    ? trainee.getUser().getUsername() : "";
        }
        if (entity instanceof Trainer trainer && trainer.getUser() != null) {
            return trainer.getUser().getUsername() != null
                    ? trainer.getUser().getUsername() : "";
        }
        return "";
    }
}
