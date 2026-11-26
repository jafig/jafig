package com.badminimum.jafig.api.config.node.builder;

import com.badminimum.jafig.api.config.env.EnvAdapter;
import com.badminimum.jafig.api.config.node.value.ValueNode;
import com.badminimum.jafig.api.config.serialization.JafigSerializer;
import com.badminimum.jafig.api.config.validator.Validator;
import com.google.common.reflect.TypeToken;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface ValueNodeBuilder<T> extends NodeBuilder {
    ValueNodeBuilder<T> defaultValue(@Nullable T defaultValue);

    ValueNodeBuilder<T> validator(Validator<@Nullable T> validator);

    ValueNodeBuilder<T> serializer(TypeToken<? extends JafigSerializer<T>> serializer);

    EnvAdapterStep<T> env(String variable);

    interface EnvAdapterStep<T> extends ValueNodeBuilder<T>, BuildStep<T> {
        EnvAdapterStep<T> adapter(EnvAdapter<T> adapter);
    }

    interface BuildStep<T> {
        ValueNode<T> build();
    }
}