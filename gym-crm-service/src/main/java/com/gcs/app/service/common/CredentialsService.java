package com.gcs.app.service.common;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CredentialsService {


    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+-=[]{};':\"\\|,.<>/?";
    private static final String ALL = UPPER + LOWER + DIGITS + SPECIAL;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public String generateRandomPassword() {
        int desiredLength = 10;
        SecureRandom random = new SecureRandom();
        List<Character> passwordChars = new ArrayList<>();

        passwordChars.add(randomCharFrom(UPPER, random));
        passwordChars.add(randomCharFrom(LOWER, random));
        passwordChars.add(randomCharFrom(DIGITS, random));
        passwordChars.add(randomCharFrom(SPECIAL, random));

        for (int i = passwordChars.size(); i < desiredLength; i++) {
            passwordChars.add(randomCharFrom(ALL, random));
        }

        Collections.shuffle(passwordChars, random);

        return passwordChars.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public String generateUsername(String firstName, String lastName, Set<String> existingUsernames) {
        String baseUsername = firstName + "." + lastName;
        String username = baseUsername;
        int suffix = 1;

        while (isUsernameExists(username, existingUsernames)) {
            username = baseUsername + suffix++;
        }

        return username;
    }

    public boolean isPasswordCorrect(String rawPassword, String encodedPassword) {
        return passwordEncoder.matches(rawPassword, encodedPassword);
    }

    private boolean isUsernameExists(String username, Set<String> existingUsernames) {
        if (existingUsernames.isEmpty()) {
            return false;
        }

        return existingUsernames.stream()
                .anyMatch(existing -> existing.equalsIgnoreCase(username));
    }

    private char randomCharFrom(String chars, Random random) {
        return chars.charAt(random.nextInt(chars.length()));
    }
}
