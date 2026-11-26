package com.badminimum.jafig.api.config.node.value;

import com.badminimum.jafig.api.config.env.EnvBinding;
import com.badminimum.jafig.api.config.node.Node;
import com.badminimum.jafig.api.config.node.path.TypedNodePath;
import com.badminimum.jafig.api.config.serialization.JafigSerializer;
import com.badminimum.jafig.api.config.validator.Validator;
import com.google.common.reflect.TypeToken;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public non-sealed interface ValueNode<T> extends Node {
    @Override
    TypedNodePath<T> path();

    Optional<T> value();

    Optional<T> defaultValue();

    default Optional<T> valueOrDefault() {
        return this.value().or(this::defaultValue);
    }

    Validator<@Nullable T> validator();

    Optional<TypeToken<? extends JafigSerializer<T>>> serializer();

    Optional<EnvBinding<@Nullable T>> env();
}