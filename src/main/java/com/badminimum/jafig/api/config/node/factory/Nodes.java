package com.badminimum.jafig.api.config.node.factory;

import com.badminimum.jafig.api.config.node.builder.ValueNodeBuilder;
import com.badminimum.jafig.api.config.node.path.NodePath;
import com.badminimum.jafig.impl.config.node.value.ValueNodeBuilderImpl;
import com.google.common.reflect.TypeToken;

public interface Nodes {
    static <T> ValueNodeBuilder<T> value(NodePath nodePath, TypeToken<T> typeToken) {
        return new ValueNodeBuilderImpl<>(nodePath, typeToken);
    }
}