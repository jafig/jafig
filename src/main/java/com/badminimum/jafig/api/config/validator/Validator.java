package com.badminimum.jafig.api.config.validator;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

@FunctionalInterface
@NullMarked
public interface Validator<T extends @Nullable Object> {
    static <T> Validator<@Nullable T> of(Predicate<? super @Nullable T> predicate, String message) {
        return (@Nullable T value, ValidationContext ctx) -> {
            if (!predicate.test(value)) {
                ctx.error(message, (value == null) ? "null" : value);
            }
        };
    }

    default Validator<@Nullable T> and(Validator<? super @Nullable T> other) {
        return (@Nullable T value, ValidationContext ctx) -> {
            this.validate(value, ctx);
            other.validate(value, ctx);
        };
    }

    void validate(@Nullable T value, ValidationContext context);
}
