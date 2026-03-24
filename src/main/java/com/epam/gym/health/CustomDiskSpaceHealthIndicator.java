package com.epam.gym.health;

import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component("customDiskSpace")
public class CustomDiskSpaceHealthIndicator implements HealthIndicator {

    private static final long THRESHOLD_BYTES = 100 * 1024 * 1024;

    @Override
    public Health health() {
        File disk = new File("/");
        long freeSpace = disk.getFreeSpace();
        long totalSpace = disk.getTotalSpace();
        long usedSpace = totalSpace - freeSpace;
        double usedPercent = (double) usedSpace / totalSpace * 100;

        log.debug("Disk space check - Free: {} MB", freeSpace / (1024 * 1024));

        if (freeSpace < THRESHOLD_BYTES) {
            return Health.down()
                    .withDetail("error", "Low disk space")
                    .withDetail("free", formatBytes(freeSpace))
                    .withDetail("threshold", formatBytes(THRESHOLD_BYTES))
                    .build();
        }

        return Health.up()
                .withDetail("total", formatBytes(totalSpace))
                .withDetail("free", formatBytes(freeSpace))
                .withDetail("used", formatBytes(usedSpace))
                .withDetail("usedPercent", String.format("%.2f%%", usedPercent))
                .build();
    }

    private String formatBytes(long bytes) {
        if (bytes >= 1073741824) {
            return String.format("%.2f GB", bytes / 1073741824.0);
        } else if (bytes >= 1048576) {
            return String.format("%.2f MB", bytes / 1048576.0);
        }
        return String.format("%.2f KB", bytes / 1024.0);
    }
}