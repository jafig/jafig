package com.badminimum.jafig.api.config.node.path;

import com.badminimum.jafig.impl.config.node.path.AbstractNodePath;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

@NullMarked
public sealed interface NodePath permits TypedNodePath, StructuralNodePath, AbstractNodePath {
    ImmutableList<String> keys();

    default Optional<StructuralNodePath> parent() {
        return this.isRoot()
                ? Optional.empty()
                : Optional.of(NodePaths.from(StructuralNodePath.creator(), this.keys().subList(0, this.depth() - 1)));
    }

    default boolean isRoot() {
        return this.keys().isEmpty();
    }

    default int depth() {
        return this.keys().size();
    }

    default Optional<String> name() {
        return this.isRoot() ? Optional.empty() : Optional.of(this.keys().getLast());
    }
}
