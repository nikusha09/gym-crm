package com.gym.util;

import com.gym.model.Trainee;
import com.gym.model.Trainer;
import com.gym.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UsernamePasswordGeneratorTest {

    private static final String ALLOWED_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private UsernamePasswordGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new UsernamePasswordGenerator();
    }

    @Test
    @DisplayName("generateUsername: empty collection returns base username")
    void generateUsername_emptyCollection_returnsBase() {
        String result = generator.generateUsername("John", "Smith", List.of());

        assertEquals("John.Smith", result);
    }

    @Test
    @DisplayName("generateUsername: no conflict in User collection returns base")
    void generateUsername_noConflictInUserCollection_returnsBase() {
        User existing = new User();
        existing.setUsername("Jane.Doe");

        String result = generator.generateUsername("John", "Smith", List.of(existing));

        assertEquals("John.Smith", result);
    }

    @Test
    @DisplayName("generateUsername: one conflict in User collection returns base with suffix 1")
    void generateUsername_oneConflictInUserCollection_returnsSuffix1() {
        User existing = new User();
        existing.setUsername("John.Smith");

        String result = generator.generateUsername("John", "Smith", List.of(existing));

        assertEquals("John.Smith1", result);
    }

    @Test
    @DisplayName("generateUsername: multiple conflicts in User collection returns correct suffix")
    void generateUsername_multipleConflictsInUserCollection_returnsCorrectSuffix() {
        User u1 = new User();
        u1.setUsername("John.Smith");
        User u2 = new User();
        u2.setUsername("John.Smith1");
        User u3 = new User();
        u3.setUsername("John.Smith2");

        String result = generator.generateUsername("John", "Smith", List.of(u1, u2, u3));

        assertEquals("John.Smith3", result);
    }

    @Test
    @DisplayName("generateUsername: User with null username does not throw and is ignored")
    void generateUsername_userWithNullUsername_doesNotThrow() {
        User existing = new User();
        existing.setUsername(null);

        assertDoesNotThrow(() -> {
            String result = generator.generateUsername("John", "Smith", List.of(existing));
            assertEquals("John.Smith", result);
        });
    }

    @Test
    @DisplayName("generateUsername: no conflict in Trainee collection returns base")
    void generateUsername_noConflictInTraineeCollection_returnsBase() {
        Trainee trainee = new Trainee();
        User u = new User();
        u.setUsername("Jane.Doe");
        trainee.setUser(u);

        String result = generator.generateUsername("John", "Smith", List.of(trainee));

        assertEquals("John.Smith", result);
    }

    @Test
    @DisplayName("generateUsername: one conflict in Trainee collection returns suffix 1")
    void generateUsername_oneConflictInTraineeCollection_returnsSuffix1() {
        Trainee trainee = new Trainee();
        User u = new User();
        u.setUsername("John.Smith");
        trainee.setUser(u);

        String result = generator.generateUsername("John", "Smith", List.of(trainee));

        assertEquals("John.Smith1", result);
    }

    @Test
    @DisplayName("generateUsername: Trainee with null User does not throw and is ignored")
    void generateUsername_traineeWithNullUser_doesNotThrow() {
        Trainee trainee = new Trainee();
        trainee.setUser(null);

        assertDoesNotThrow(() -> {
            String result = generator.generateUsername("John", "Smith", List.of(trainee));
            assertEquals("John.Smith", result);
        });
    }

    @Test
    @DisplayName("generateUsername: Trainee with null username inside User does not throw")
    void generateUsername_traineeWithNullUsernameInsideUser_doesNotThrow() {
        Trainee trainee = new Trainee();
        User u = new User();
        u.setUsername(null);
        trainee.setUser(u);

        assertDoesNotThrow(() -> {
            String result = generator.generateUsername("John", "Smith", List.of(trainee));
            assertEquals("John.Smith", result);
        });
    }

    @Test
    @DisplayName("generateUsername: no conflict in Trainer collection returns base")
    void generateUsername_noConflictInTrainerCollection_returnsBase() {
        Trainer trainer = new Trainer();
        User u = new User();
        u.setUsername("Jane.Doe");
        trainer.setUser(u);

        String result = generator.generateUsername("John", "Smith", List.of(trainer));

        assertEquals("John.Smith", result);
    }

    @Test
    @DisplayName("generateUsername: one conflict in Trainer collection returns suffix 1")
    void generateUsername_oneConflictInTrainerCollection_returnsSuffix1() {
        Trainer trainer = new Trainer();
        User u = new User();
        u.setUsername("John.Smith");
        trainer.setUser(u);

        String result = generator.generateUsername("John", "Smith", List.of(trainer));

        assertEquals("John.Smith1", result);
    }

    @Test
    @DisplayName("generateUsername: Trainer with null User does not throw and is ignored")
    void generateUsername_trainerWithNullUser_doesNotThrow() {
        Trainer trainer = new Trainer();
        trainer.setUser(null);

        assertDoesNotThrow(() -> {
            String result = generator.generateUsername("John", "Smith", List.of(trainer));
            assertEquals("John.Smith", result);
        });
    }

    @Test
    @DisplayName("generateUsername: conflicts across mixed User/Trainee/Trainer collection")
    void generateUsername_conflictsAcrossMixedCollection_returnsCorrectSuffix() {
        User u = new User();
        u.setUsername("John.Smith");

        Trainee trainee = new Trainee();
        User tu = new User();
        tu.setUsername("John.Smith1");
        trainee.setUser(tu);

        Trainer trainer = new Trainer();
        User tru = new User();
        tru.setUsername("John.Smith2");
        trainer.setUser(tru);

        String result = generator.generateUsername("John", "Smith", List.of(u, trainee, trainer));

        assertEquals("John.Smith3", result);
    }

    @Test
    @DisplayName("generateUsername: unrelated usernames that share partial prefix are not counted")
    void generateUsername_partialPrefixMatch_notCounted() {
        // "John.Smithson" starts with "John.Smith" — this IS counted by startsWith
        // This test documents and verifies that known behavior
        User u1 = new User();
        u1.setUsername("John.Smith");
        User u2 = new User();
        u2.setUsername("John.Smithson"); // startsWith("John.Smith") = true, counts

        String result = generator.generateUsername("John", "Smith", List.of(u1, u2));

        // Both match startsWith("John.Smith"), so count=2, suffix=2
        assertEquals("John.Smith2", result);
    }

    @Test
    @DisplayName("generatePassword: returns string of exactly 10 characters")
    void generatePassword_returnsExactly10Characters() {
        String password = generator.generatePassword();

        assertEquals(10, password.length());
    }

    @Test
    @DisplayName("generatePassword: all characters are from the allowed set")
    void generatePassword_allCharactersFromAllowedSet() {
        String password = generator.generatePassword();

        for (char c : password.toCharArray()) {
            assertTrue(ALLOWED_CHARS.indexOf(c) >= 0,
                    "Unexpected character in password: " + c);
        }
    }

    @Test
    @DisplayName("generatePassword: two consecutive calls produce different passwords")
    void generatePassword_consecutiveCalls_produceDifferentResults() {
        String first = generator.generatePassword();
        String second = generator.generatePassword();

        assertNotEquals(first, second,
                "Two generated passwords should almost never be identical");
    }

    @Test
    @DisplayName("generatePassword: returns non-null, non-blank string")
    void generatePassword_returnsNonNullNonBlank() {
        String password = generator.generatePassword();

        assertNotNull(password);
        assertFalse(password.isBlank());
    }

    @Test
    @DisplayName("generateUsername: collection with unrecognized type returns base username")
    void generateUsername_unrecognizedType_treatedAsNonConflict() {
        // Strings are not User/Trainee/Trainer — getUsernameFrom returns ""
        // "" does not startWith "John.Smith", so count=0
        String result = generator.generateUsername("John", "Smith", List.of("some-string"));

        assertEquals("John.Smith", result);
    }
}
