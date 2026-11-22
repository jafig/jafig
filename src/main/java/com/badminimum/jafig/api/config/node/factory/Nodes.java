package com.badminimum.jafig.api.config.node.factory;

import com.badminimum.jafig.api.config.node.path.NodePath;
import com.badminimum.jafig.api.config.node.value.ValueNode;
import com.badminimum.jafig.api.config.node.value.ValueNodeBuilder;
import com.badminimum.jafig.impl.config.node.value.builder.ValueNodeBuilderImpl;
import com.badminimum.jafig.impl.utils.typetoken.TypeTokenUtils;
import com.google.common.reflect.TypeToken;

public interface Nodes {
    static <T> ValueNode<T> value(TypeToken<T> typeToken, NodePath nodePath, T defaultValue) {
        // TODO
        return null;
    }

    static <T> ValueNode<T> value(Class<T> simpleType, NodePath nodePath, T defaultValue) {
        return value(TypeTokenUtils.getSimpleOrThrow(simpleType), nodePath, defaultValue);
    }

    static <T> ValueNode<T> emptyValue(TypeToken<T> typeToken, NodePath nodePath) {
        return value(typeToken, nodePath, null);
    }

    static <T> ValueNode<T> emptyValue(Class<T> simpleType, NodePath nodePath) {
        return emptyValue(TypeTokenUtils.getSimpleOrThrow(simpleType), nodePath);
    }

    static <T> ValueNodeBuilder.Default<T> value(TypeToken<T> typeToken, NodePath nodePath) {
        return new ValueNodeBuilderImpl<>(typeToken, nodePath);
    }

    static <T> ValueNodeBuilder.Default<T> value(Class<T> simpleType, NodePath nodePath) {
        return value(TypeTokenUtils.getSimpleOrThrow(simpleType), nodePath);
    }
}
