package com.badminimum.jafig.api.config.node.builder;

import com.badminimum.jafig.api.config.node.group.GroupNode;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ListNodeBuilder<T> extends NodeBuilder {
    GroupNode.List build();
}