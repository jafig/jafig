package com.badminimum.jafig.api.config.node.path;

import com.badminimum.jafig.impl.config.node.path.TypedNodePathImpl;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import org.jspecify.annotations.NullMarked;

@NullMarked
public non-sealed interface TypedNodePath<T> extends NodePath {
    static <T> NodePaths.Creator<TypedNodePath<T>> creator(TypeToken<T> typeToken) {
        return (StructuralNodePath parent, ImmutableList<String> keys) -> new TypedNodePathImpl<>(typeToken,
                parent.child(keys).keys());
    }

    static <T> TypedNodePath<T> of(NodePath path, TypeToken<T> type) {
        return NodePaths.from(creator(type), path.keys());
    }

    TypeToken<T> typeToken();
}
