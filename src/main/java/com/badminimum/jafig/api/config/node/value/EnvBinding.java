package com.badminimum.jafig.api.config.node.value;

import com.badminimum.jafig.api.config.env.EnvAdapter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record EnvBinding<T>(String variable, @Nullable EnvAdapter<T> adapter) {
}
