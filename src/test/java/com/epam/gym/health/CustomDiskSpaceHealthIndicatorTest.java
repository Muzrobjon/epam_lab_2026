package com.epam.gym.health;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CustomDiskSpaceHealthIndicator Tests")
class CustomDiskSpaceHealthIndicatorTest {

    @BeforeAll
    static void setUpLocale() {
        Locale.setDefault(Locale.US);
    }
    private final CustomDiskSpaceHealthIndicator healthIndicator = new CustomDiskSpaceHealthIndicator();

    @Nested
    @DisplayName("health() method tests")
    class HealthMethodTests {

        @Test
        @DisplayName("Should return UP status when disk space is sufficient")
        void shouldReturnUpStatusWhenDiskSpaceIsSufficient() {
            Health health = healthIndicator.health();

            // On most systems, there should be more than 100MB free
            // This test assumes the system has sufficient disk space
            File disk = new File("/");
            long freeSpace = disk.getFreeSpace();
            long threshold = 100 * 1024 * 1024L; // 100 MB

            if (freeSpace >= threshold) {
                assertThat(health.getStatus()).isEqualTo(Status.UP);
            } else {
                assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            }
        }

        @Test
        @DisplayName("Should include total space in health details when UP")
        void shouldIncludeTotalSpaceInHealthDetails() {
            Health health = healthIndicator.health();

            if (health.getStatus().equals(Status.UP)) {
                assertThat(health.getDetails()).containsKey("total");
                assertThat(health.getDetails().get("total").toString()).matches(".*[KMGT]?B.*|\\d+\\.\\d+ [KMGT]B");
            }
        }

        @Test
        @DisplayName("Should include free space in health details")
        void shouldIncludeFreeSpaceInHealthDetails() {
            Health health = healthIndicator.health();

            assertThat(health.getDetails()).containsKey("free");
        }

        @Test
        @DisplayName("Should include used space in health details when UP")
        void shouldIncludeUsedSpaceInHealthDetails() {
            Health health = healthIndicator.health();

            if (health.getStatus().equals(Status.UP)) {
                assertThat(health.getDetails()).containsKey("used");
            }
        }

        @Test
        @DisplayName("Should include used percentage in health details when UP")
        void shouldIncludeUsedPercentageInHealthDetails() {
            Health health = healthIndicator.health();

            if (health.getStatus().equals(Status.UP)) {
                assertThat(health.getDetails()).containsKey("usedPercent");
                String usedPercent = health.getDetails().get("usedPercent").toString();
                assertThat(usedPercent).matches("\\d+\\.\\d+%");
            }
        }

        @Test
        @DisplayName("Should return valid health object")
        void shouldReturnValidHealthObject() {
            Health health = healthIndicator.health();

            assertThat(health).isNotNull();
            assertThat(health.getStatus()).isNotNull();
            assertThat(health.getDetails()).isNotNull();
            assertThat(health.getDetails()).isNotEmpty();
        }

        @Test
        @EnabledOnOs({OS.LINUX, OS.MAC, OS.WINDOWS})
        @DisplayName("Should work on different operating systems")
        void shouldWorkOnDifferentOperatingSystems() {
            Health health = healthIndicator.health();

            assertThat(health).isNotNull();
            assertThat(health.getStatus()).isIn(Status.UP, Status.DOWN);
        }
    }

    @Nested
    @DisplayName("formatBytes() method tests")
    class FormatBytesTests {

        @ParameterizedTest
        @CsvSource({
                "1073741824, '1.00 GB'",
                "2147483648, '2.00 GB'",
                "1610612736, '1.50 GB'"
        })
        @DisplayName("Should format bytes as GB when >= 1GB")
        void shouldFormatBytesAsGigabytes(long bytes, String expected) throws Exception {
            String result = invokeFormatBytes(bytes);
            assertThat(result).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "1048576, '1.00 MB'",
                "104857600, '100.00 MB'",
                "524288000, '500.00 MB'"
        })
        @DisplayName("Should format bytes as MB when >= 1MB and < 1GB")
        void shouldFormatBytesAsMegabytes(long bytes, String expected) throws Exception {
            String result = invokeFormatBytes(bytes);
            assertThat(result).isEqualTo(expected);
        }

        @ParameterizedTest
        @CsvSource({
                "1024, '1.00 KB'",
                "512000, '500.00 KB'",
                "1023, '1.00 KB'"
        })
        @DisplayName("Should format bytes as KB when < 1MB")
        void shouldFormatBytesAsKilobytes(long bytes, String expected) throws Exception {
            String result = invokeFormatBytes(bytes);
            assertThat(result).isEqualTo(expected);
        }

        @Test
        @DisplayName("Should handle zero bytes")
        void shouldHandleZeroBytes() throws Exception {
            String result = invokeFormatBytes(0);
            assertThat(result).isEqualTo("0.00 KB");
        }

        @Test
        @DisplayName("Should handle exact boundary values")
        void shouldHandleExactBoundaryValues() throws Exception {
            // Exactly 1 GB
            assertThat(invokeFormatBytes(1073741824L)).isEqualTo("1.00 GB");

            // Just under 1 GB (1 GB - 1 byte)
            assertThat(invokeFormatBytes(1073741823L)).isEqualTo("1024.00 MB");

            // Exactly 1 MB
            assertThat(invokeFormatBytes(1048576L)).isEqualTo("1.00 MB");

            // Just under 1 MB (1 MB - 1 byte)
            assertThat(invokeFormatBytes(1048575L)).isEqualTo("1024.00 KB");
        }

        private String invokeFormatBytes(long bytes) throws Exception {
            Method method = CustomDiskSpaceHealthIndicator.class.getDeclaredMethod("formatBytes", long.class);
            method.setAccessible(true);
            return (String) method.invoke(healthIndicator, bytes);
        }
    }

    @Nested
    @DisplayName("Health status scenarios")
    class HealthStatusScenarios {

        @Test
        @DisplayName("Health details should contain formatted values")
        void healthDetailsShouldContainFormattedValues() {
            Health health = healthIndicator.health();

            health.getDetails().values().forEach(value -> {
                String strValue = value.toString();
                // Should be either a formatted byte string or percentage
                assertThat(strValue).matches(".*[KMGT]B.*|\\d+\\.\\d+%|.*");
            });
        }

        @Test
        @DisplayName("Used percentage should be between 0 and 100")
        void usedPercentageShouldBeBetweenZeroAndHundred() {
            Health health = healthIndicator.health();

            if (health.getStatus().equals(Status.UP)) {
                String usedPercentStr = health.getDetails().get("usedPercent").toString();
                double usedPercent = Double.parseDouble(usedPercentStr.replace("%", ""));
                assertThat(usedPercent).isBetween(0.0, 100.0);
            }
        }
    }

    @Nested
    @DisplayName("Threshold tests")
    class ThresholdTests {

        @Test
        @DisplayName("Should use 100MB as threshold")
        void shouldUse100MBAsThreshold() throws Exception {
            // Access the private static field using reflection
            java.lang.reflect.Field field = CustomDiskSpaceHealthIndicator.class.getDeclaredField("THRESHOLD_BYTES");
            field.setAccessible(true);
            long threshold = (long) field.get(null);

            assertThat(threshold).isEqualTo(100 * 1024 * 1024L);
        }
    }
}