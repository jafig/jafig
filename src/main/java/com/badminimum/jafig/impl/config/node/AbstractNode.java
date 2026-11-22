package com.badminimum.jafig.impl.config.node;

import com.badminimum.jafig.api.config.node.Node;
import com.badminimum.jafig.api.config.node.path.NodePath;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;

@NullMarked
public abstract non-sealed class AbstractNode implements Node {
    protected final NodePath nodePath;
    private final Collection<String> comments;

    public AbstractNode(NodePath nodePath, Collection<String> comments) {
        this.nodePath = nodePath;
        this.comments = comments;
    }

    @Override
    public NodePath path() {
        return this.nodePath;
    }

    @Override
    public Collection<String> comments() {
        return this.comments;
    }
}
