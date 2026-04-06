package com.epam.gym.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordGenerator passwordGenerator;

    @InjectMocks
    private PasswordService passwordService;

    // ==================== GENERATE RANDOM PASSWORD TESTS ====================

    @Nested
    @DisplayName("Generate Random Password Tests")
    class GenerateRandomPasswordTests {

        @Test
        @DisplayName("Should generate random password using PasswordGenerator")
        void generateRandomPassword_Success() {
            // Given
            String generatedPassword = "Abc@1234";
            when(passwordGenerator.generatePassword()).thenReturn(generatedPassword);

            // When
            String result = passwordService.generateRandomPassword();

            // Then
            assertThat(result).isEqualTo(generatedPassword);
            verify(passwordGenerator).generatePassword();
        }

        @Test
        @DisplayName("Should delegate to PasswordGenerator")
        void generateRandomPassword_DelegatesToGenerator() {
            // Given
            when(passwordGenerator.generatePassword()).thenReturn("anyPassword");

            // When
            passwordService.generateRandomPassword();

            // Then
            verify(passwordGenerator, times(1)).generatePassword();
            verifyNoInteractions(passwordEncoder);
        }
    }

    // ==================== ENCODE PASSWORD TESTS ====================

    @Nested
    @DisplayName("Encode Password Tests")
    class EncodePasswordTests {

        @Test
        @DisplayName("Should encode password successfully")
        void encodePassword_Success() {
            // Given
            String rawPassword = "TestPassword@123";
            String encodedPassword = "$2a$12$encodedHashValue";

            when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

            // When
            String result = passwordService.encodePassword(rawPassword);

            // Then
            assertThat(result).isEqualTo(encodedPassword);
            verify(passwordEncoder).encode(rawPassword);
        }

        @Test
        @DisplayName("Should delegate to PasswordEncoder")
        void encodePassword_DelegatesToEncoder() {
            // Given
            when(passwordEncoder.encode(anyString())).thenReturn("encoded");

            // When
            passwordService.encodePassword("password");

            // Then
            verify(passwordEncoder, times(1)).encode("password");
            verifyNoInteractions(passwordGenerator);
        }
    }

    // ==================== MATCHES TESTS ====================

    @Nested
    @DisplayName("Matches Tests")
    class MatchesTests {

        @Test
        @DisplayName("Should return true when passwords match")
        void matches_PasswordsMatch_ReturnsTrue() {
            // Given
            String rawPassword = "TestPassword@123";
            String encodedPassword = "$2a$12$encodedHashValue";

            when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

            // When
            boolean result = passwordService.matches(rawPassword, encodedPassword);

            // Then
            assertThat(result).isTrue();
            verify(passwordEncoder).matches(rawPassword, encodedPassword);
        }

        @Test
        @DisplayName("Should return false when passwords don't match")
        void matches_PasswordsDontMatch_ReturnsFalse() {
            // Given
            String rawPassword = "WrongPassword";
            String encodedPassword = "$2a$12$encodedHashValue";

            when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

            // When
            boolean result = passwordService.matches(rawPassword, encodedPassword);

            // Then
            assertThat(result).isFalse();
            verify(passwordEncoder).matches(rawPassword, encodedPassword);
        }

        @Test
        @DisplayName("Should delegate to PasswordEncoder")
        void matches_DelegatesToEncoder() {
            // Given
            when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

            // When
            passwordService.matches("raw", "encoded");

            // Then
            verify(passwordEncoder, times(1)).matches("raw", "encoded");
            verifyNoInteractions(passwordGenerator);
        }
    }

    // ==================== IS PASSWORD STRONG TESTS ====================

    @Nested
    @DisplayName("Is Password Strong Tests")
    class IsPasswordStrongTests {

        @Test
        @DisplayName("Should return true for strong password")
        void isPasswordStrong_StrongPassword_ReturnsTrue() {
            assertThat(passwordService.isPasswordStrong("Abc@1234")).isTrue();
        }

        @Test
        @DisplayName("Should return true for password with all required characters")
        void isPasswordStrong_AllRequiredChars_ReturnsTrue() {
            assertThat(passwordService.isPasswordStrong("Test$567")).isTrue();
        }

        @ParameterizedTest
        @DisplayName("Should return true for various strong passwords")
        @ValueSource(strings = {
                "Abcdef@1",
                "Password$1",
                "Hello&12World",
                "Str0ng#Pass",
                "MyP@ss123"
        })
        void isPasswordStrong_VariousStrongPasswords_ReturnsTrue(String password) {
            assertThat(passwordService.isPasswordStrong(password)).isTrue();
        }

        // ---------- NULL AND SHORT PASSWORDS ----------

        @Test
        @DisplayName("Should return false for null password")
        void isPasswordStrong_NullPassword_ReturnsFalse() {
            assertThat(passwordService.isPasswordStrong(null)).isFalse();
        }

        @Test
        @DisplayName("Should return false for empty password")
        void isPasswordStrong_EmptyPassword_ReturnsFalse() {
            assertThat(passwordService.isPasswordStrong("")).isFalse();
        }

        @Test
        @DisplayName("Should return false for password shorter than 8 characters")
        void isPasswordStrong_TooShort_ReturnsFalse() {
            assertThat(passwordService.isPasswordStrong("Ab@1")).isFalse();
        }

        @Test
        @DisplayName("Should return false for exactly 7 characters")
        void isPasswordStrong_SevenChars_ReturnsFalse() {
            assertThat(passwordService.isPasswordStrong("Ab@1234")).isFalse();
        }

        // ---------- MISSING UPPERCASE ----------

        @Test
        @DisplayName("Should return false when missing uppercase")
        void isPasswordStrong_NoUppercase_ReturnsFalse() {
            assertThat(passwordService.isPasswordStrong("abcdef@1")).isFalse();
        }

        // ---------- MISSING LOWERCASE ----------

        @Test
        @DisplayName("Should return false when missing lowercase")
        void isPasswordStrong_NoLowercase_ReturnsFalse() {
            assertThat(passwordService.isPasswordStrong("ABCDEF@1")).isFalse();
        }

        // ---------- MISSING DIGIT ----------

        @Test
        @DisplayName("Should return false when missing digit")
        void isPasswordStrong_NoDigit_ReturnsFalse() {
            assertThat(passwordService.isPasswordStrong("Abcdefg@")).isFalse();
        }

        // ---------- MISSING SPECIAL CHARACTER ----------

        @Test
        @DisplayName("Should return false when missing special character")
        void isPasswordStrong_NoSpecialChar_ReturnsFalse() {
            assertThat(passwordService.isPasswordStrong("Abcdef12")).isFalse();
        }

        // ---------- SPECIAL CHARACTERS VALIDATION ----------

        @ParameterizedTest
        @DisplayName("Should accept valid special characters")
        @ValueSource(strings = {
                "Abcdef$1",
                "Abcdef%1",
                "Abcdef&1",
                "Abcdef@1",
                "Abcdef#1"
        })
        void isPasswordStrong_ValidSpecialChars_ReturnsTrue(String password) {
            assertThat(passwordService.isPasswordStrong(password)).isTrue();
        }

        @Test
        @DisplayName("Should return false for unsupported special character")
        void isPasswordStrong_UnsupportedSpecialChar_ReturnsFalse() {
            assertThat(passwordService.isPasswordStrong("Abcdef!1")).isFalse();
        }

        @ParameterizedTest
        @DisplayName("Should return false for unsupported special characters")
        @ValueSource(strings = {
                "Abcdef!1",
                "Abcdef^1",
                "Abcdef*1",
                "Abcdef(1",
                "Abcdef~1"
        })
        void isPasswordStrong_UnsupportedSpecialChars_ReturnsFalse(String password) {
            assertThat(passwordService.isPasswordStrong(password)).isFalse();
        }

        // ---------- NO SERVICE INTERACTION ----------

        @Test
        @DisplayName("Should not interact with any dependencies")
        void isPasswordStrong_NoServiceInteraction() {
            passwordService.isPasswordStrong("Abc@1234");

            verifyNoInteractions(passwordEncoder);
            verifyNoInteractions(passwordGenerator);
        }
    }
}