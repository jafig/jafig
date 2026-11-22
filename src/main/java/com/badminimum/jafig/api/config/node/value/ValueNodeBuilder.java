package com.badminimum.jafig.api.config.node.value;

import com.badminimum.jafig.api.config.env.EnvAdapter;
import com.badminimum.jafig.api.config.validator.Validator;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

@NullMarked
public interface ValueNodeBuilder<T> {
    ValueNodeBuilder<T> validator(Validator<@Nullable T> validator);

    ValueNodeBuilder<T> defaultValue(@Nullable T defaultValue);

    ValueNodeBuilder<T> comments(String... comments);

    ValueNodeBuilder<T> comments(Collection<String> comments);

    EnvAdapterStep<T> env(String variable);

    interface EnvAdapterStep<T> extends ValueNodeBuilder<T>, BuildStep<T> {
        EnvAdapterStep<T> adapter(EnvAdapter<T> adapter);
    }

    interface BuildStep<T> {
        ValueNode<T> build();
    }
}