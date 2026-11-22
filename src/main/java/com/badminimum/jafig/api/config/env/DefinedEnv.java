package com.badminimum.jafig.api.config.env;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

@NullMarked
public record DefinedEnv<T extends @Nullable Object>(String key, Function<String, @Nullable T> adapter) {
    static DefinedEnv<@Nullable Path> path(String key) {
        return new DefinedEnv<>(key, (String input) -> {
            try {
                return Path.of(input).toRealPath();
            } catch (IllegalArgumentException | IOException ex) {
                return null;
            }
        });
    }

}
