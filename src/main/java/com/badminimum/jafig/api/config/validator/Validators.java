package com.badminimum.jafig.api.config.validator;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NullMarked
public final class Validators {
    private Validators() {
    }

    public static <T> Validator<T> empty() {
        return (T ignored, ValidationContext ignored2) -> {};
    }

    public static <T> Validator<@Nullable T> notNull() {
        return (@Nullable T item, ValidationContext ctx) -> {
            if (item != null) {
                return;
            }
            ctx.error("item is null");
        };
    }

    public static <T extends Comparable<? super T>> Validator<T> min(T minimum) {
        return (T item, ValidationContext ctx) -> {
            if (item.compareTo(minimum) >= 0) {
                return;
            }
            ctx.error("%s must be at least %s", item.toString(), minimum.toString());
        };
    }

    public static <T extends Comparable<? super T>> Validator<T> max(T maximum) {
        return (T item, ValidationContext ctx) -> {
            if (item.compareTo(maximum) <= 0) {
                return;
            }
            ctx.error("%s must be at most %s", item.toString(), maximum.toString());
        };
    }

    public static <T extends Comparable<? super T>> Validator<T> range(T min, T max) {
        return (T item, ValidationContext ctx) -> {
            if ((item.compareTo(min) >= 0) && (item.compareTo(max) <= 0)) {
                return;
            }

            ctx.error("%s must be between %s and %s (inclusive)", item.toString(), min.toString(), max.toString());
        };
    }

    public static Validator<String> notBlank() {
        return (String text, ValidationContext ctx) -> {
            if (!text.isBlank()) {
                return;
            }
            ctx.error("value must not be blank");
        };
    }

    public static Validator<String> matches(Pattern pattern) {
        return (String text, ValidationContext ctx) -> {
            final Matcher matcher = pattern.matcher(text);
            if (matcher.matches()) {
                return;
            }
            ctx.error("%s must match the pattern '%s'", text, pattern.pattern());
        };
    }

    @SafeVarargs
    public static <T> Validator<T> oneOf(T... items) {
        return oneOf(Arrays.asList(items));
    }

    public static <T> Validator<T> oneOf(Collection<? extends T> items) {
        return (T item, ValidationContext ctx) -> {
            if (items.contains(item)) {
                return;
            }
            ctx.error("%s must be one of %s", item.toString(), Arrays.toString(items.toArray()));
        };
    }

    @SafeVarargs
    public static <T> Validator<T> notIn(T... items) {
        return notIn(Arrays.asList(items));
    }

    public static <T> Validator<T> notIn(Collection<? extends T> items) {
        return (T item, ValidationContext ctx) -> {
            if (!items.contains(item)) {
                return;
            }
            ctx.error("%s must not be one of %s", item.toString(), Arrays.toString(items.toArray()));
        };
    }

    public static <T> Validator<T[]> notEmptyArray() {
        return (T[] items, ValidationContext ctx) -> {
            if (items.length != 0) {
                return;
            }
            ctx.error("array must not be empty");
        };
    }

    public static <C extends Collection<?>> Validator<C> notEmpty() {
        return (C item, ValidationContext ctx) -> {
            if (!item.isEmpty()) {
                return;
            }
            ctx.error("collection must not be empty");
        };
    }
}