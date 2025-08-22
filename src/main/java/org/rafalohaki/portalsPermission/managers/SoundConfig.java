package org.rafalohaki.portalsPermission.managers;

import org.jetbrains.annotations.NotNull;

/**
 * Record for sound configuration settings
 * Record dla ustawień konfiguracji dźwięku
 */
public record SoundConfig(
    boolean enabled,
    @NotNull String type,
    float volume,
    float pitch
) {
    public SoundConfig {
        if (volume < 0.0f || volume > 1.0f) {
            throw new IllegalArgumentException("Volume must be between 0.0 and 1.0");
        }
        if (pitch < 0.0f || pitch > 2.0f) {
            throw new IllegalArgumentException("Pitch must be between 0.0 and 2.0");
        }
    }
}