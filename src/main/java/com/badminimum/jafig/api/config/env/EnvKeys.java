package com.badminimum.jafig.api.config.env;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Stream;

// All nulls in EnvBinding are the culprits for DataFlowIssue warnings, but they are handled correctly in the code.
@NullMarked
public final class EnvKeys {

    public static final EnvBinding<String> DEBUGGER_ID = new EnvBinding<>("DEBUGGER_ID", (String t) -> t);
    public static final EnvBinding<Boolean> DEBUGGING_ENABLED = new EnvBinding<>("DEBUGGER_ENABLED",
            Boolean::parseBoolean);
    public static final EnvBinding<@Nullable Integer> DISPLAY = new EnvBinding<>("DISPLAY", (String input) -> {
        try {
            return Integer.parseInt(input.substring(1));
        } catch (NumberFormatException e) {
            return null;
        }
    });
    public static final EnvBinding<@Nullable Path> EDITOR = EnvBinding.path("EDITOR");
    public static final EnvBinding<@Nullable Path> HOME = EnvBinding.path("HOME");
    public static final EnvBinding<@Nullable LangResult> LANG = new EnvBinding<>("LANG", (String input) -> {
        String[] split = input.split("\\.", 2);
        return (split.length != 2) ? null : new LangResult(Locale.of(split[0]), split[1]);
    });
    public static final EnvBinding<@Nullable Collection<Path>> PATHS = new EnvBinding<>("PATH", (String input) -> {
        var split = input.split(";");
        try {
            return Stream.of(split).map((String stringPath) -> {
                try {
                    return Path.of(stringPath).toRealPath();
                } catch (IllegalArgumentException | IOException e) {
                    throw new RuntimeException(e);
                }
            }).toList();
        } catch (RuntimeException e) {
            return null;
        }
    });
    public static final EnvBinding<@Nullable Path> SHELL = EnvBinding.path("SHELL");
    public static final EnvBinding<String> USER = new EnvBinding<>("USER", (String t) -> t);

    private EnvKeys() {

    }
}

