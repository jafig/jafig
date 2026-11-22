package com.badminimum.jafig.api.config.validator;

import com.badminimum.jafig.api.config.node.path.NodePath;
import com.badminimum.jafig.api.config.node.path.TypedNodePath;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

@NullMarked
public interface ValidationContext {
    NodePath path();

    void error(String message, Object... args);

    void error(NodePath at, String message, Object... args);

    <V> Optional<V> get(TypedNodePath<V> other);
}