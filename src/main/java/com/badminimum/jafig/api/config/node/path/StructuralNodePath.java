package com.badminimum.jafig.api.config.node.path;

import com.badminimum.jafig.api.config.exception.NodePathException;
import com.badminimum.jafig.impl.config.node.path.StructuralNodePathImpl;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;

@NullMarked
public non-sealed interface StructuralNodePath extends NodePath {
    static NodePaths.Creator<StructuralNodePath> creator() {
        return (StructuralNodePath parent, ImmutableList<String> keys) -> new StructuralNodePathImpl(ImmutableList
                .<String>builderWithExpectedSize(parent.keys().size() + keys.size())
                .addAll(parent.keys())
                .addAll(keys)
                .build());
    }

    default StructuralNodePath child(Collection<String> more) throws NodePathException {
        return NodePaths.from(creator(), this, more);
    }

    default StructuralNodePath child(String... more) throws NodePathException {
        return NodePaths.from(creator(), this, more);
    }

    default StructuralNodePath child(StructuralNodePath other) {
        final var size = this.depth() + other.depth();

        final var builder = ImmutableList.<String>builderWithExpectedSize(size);
        builder.addAll(this.keys());
        builder.addAll(other.keys());

        return NodePaths.from(creator(), this, builder.build());
    }
}
