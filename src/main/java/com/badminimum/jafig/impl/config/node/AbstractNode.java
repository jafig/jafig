package com.badminimum.jafig.impl.config.node;

import com.badminimum.jafig.api.config.node.Node;
import com.badminimum.jafig.api.config.node.path.NodePath;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract non-sealed class AbstractNode implements Node {
    protected final NodePath nodePath;

    public AbstractNode(NodePath nodePath) {
        this.nodePath = nodePath;
    }

    @Override
    public NodePath path() {
        return this.nodePath;
    }
}
