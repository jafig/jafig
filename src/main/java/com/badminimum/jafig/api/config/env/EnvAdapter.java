package com.badminimum.jafig.api.config.env;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

@FunctionalInterface
@NullMarked
public interface EnvAdapter<T> extends Function<String, @Nullable T> {
}