package com.badminimum.jafig.api.config.node.path;

import com.badminimum.jafig.api.config.exception.NodePathException;
import com.badminimum.jafig.impl.config.node.path.NodePathImpl;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

@NullMarked
public sealed interface NodePath permits TypedNodePath, NodePathImpl {
    ImmutableList<String> keys();

    default Optional<NodePath> parent() {
        if (this.isRoot()) {
            return Optional.empty();
        }

        try {
            return Optional.of(NodePaths.of(this.keys().subList(0, this.keys().size() - 1)));
        } catch (NodePathException e) {
            return Optional.empty();
        }
    }

    default NodePath child(String... more) throws NodePathException {
        return this.child(NodePaths.of(more));
    }

    default NodePath child(NodePath other) {
        return new NodePathImpl(ImmutableList.<String>builder().addAll(this.keys()).addAll(other.keys()).build());
    }

    default boolean isRoot() {
        return this.keys().isEmpty();
    }

    default int depth() {
        return this.keys().size();
    }

    default Optional<String> name() {
        return this.isRoot() ? Optional.of("") : Optional.of(this.keys().getLast());
    }
}
