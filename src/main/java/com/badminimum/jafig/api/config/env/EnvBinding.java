package com.badminimum.jafig.api.config.env;

import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;

public record EnvBinding<T extends @Nullable Object>(String variable, @Nullable EnvAdapter<T> adapter) {
    public static EnvBinding<@Nullable Path> path(String key) {
        return new EnvBinding<>(key, (String input) -> {
            try {
                return Path.of(input).toRealPath();
            } catch (IllegalArgumentException | IOException ex) {
                return null;
            }
        });
    }
}
