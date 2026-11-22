package com.badminimum.jafig.api.config.node.path;

import com.badminimum.jafig.impl.config.node.path.TypedNodePathImpl;
import com.google.common.reflect.TypeToken;
import org.jspecify.annotations.NullMarked;

@NullMarked
public non-sealed interface TypedNodePath<T> extends NodePath {
    static <T> TypedNodePath<T> of(TypeToken<T> type, NodePath path) {
        return new TypedNodePathImpl<>(type, path.keys());
    }

    TypeToken<T> typeToken();
}
