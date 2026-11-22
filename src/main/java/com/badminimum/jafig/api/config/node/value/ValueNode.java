package com.badminimum.jafig.api.config.node.value;

import com.badminimum.jafig.api.config.node.Node;
import com.badminimum.jafig.api.config.node.path.TypedNodePath;
import com.badminimum.jafig.api.config.validator.Validator;

import java.util.Optional;

public non-sealed interface ValueNode<T> extends Node {
    @Override
    TypedNodePath<T> path();

    Optional<T> value();

    Optional<T> defaultValue();

    default Optional<T> valueOrDefault() {
        return this.value().or(this::defaultValue);
    }

    Validator<T> validator();

    Optional<EnvBinding<T>> env();
}