package com.badminimum.jafig.api.config.node;

import com.badminimum.jafig.api.config.node.group.GroupNode;
import com.badminimum.jafig.api.config.node.path.NodePath;
import com.badminimum.jafig.api.config.node.value.ValueNode;
import com.badminimum.jafig.impl.config.node.AbstractNode;

import java.util.Collection;

public sealed interface Node permits GroupNode, ValueNode, AbstractNode {
    NodePath path();

    Collection<String> comments();
}
