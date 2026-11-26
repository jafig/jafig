package com.badminimum.jafig.impl.config.node.path;

import com.badminimum.jafig.api.config.node.path.NodePath;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public abstract non-sealed class AbstractNodePath implements NodePath {
    protected final ImmutableList<String> keys;

    public AbstractNodePath(ImmutableList<String> keys) {
        this.keys = keys;
    }

    @Override
    public ImmutableList<String> keys() {
        return this.keys;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return (this == other) || ((other instanceof NodePath path) && this.keys.equals(path.keys()));
    }

    @Override
    public int hashCode() {
        return this.keys.hashCode();
    }

    @Override
    public String toString() {
        return this.isRoot() ? "" : String.join(".", this.keys);
    }
}
