package com.badminimum.jafig.impl.config.node.path;

import com.badminimum.jafig.api.config.node.path.NodePath;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NullMarked;

@NullMarked
public non-sealed class NodePathImpl implements NodePath {
    private final ImmutableList<String> keys;

    public NodePathImpl(ImmutableList<String> keys) {
        this.keys = keys;
    }

    @Override
    public ImmutableList<String> keys() {
        return this.keys;
    }

    @Override
    public String toString() {
        return this.isRoot() ? "" : String.join(".", this.keys);
    }
}
