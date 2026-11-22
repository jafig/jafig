package com.badminimum.jafig.api.config;

import com.badminimum.jafig.api.config.edit.EditResult;
import com.badminimum.jafig.api.config.edit.EditTransaction;
import com.badminimum.jafig.api.config.node.Node;
import com.badminimum.jafig.api.config.node.path.NodePath;
import com.badminimum.jafig.api.config.node.path.TypedNodePath;
import com.badminimum.jafig.api.config.node.value.ValueNode;
import com.google.common.collect.ImmutableMap;

import java.util.Optional;
import java.util.function.Consumer;

public interface Config {
    ImmutableMap<NodePath, Node> nodes();

    <T> Optional<Node> node(NodePath nodePath);

    <T> Optional<ValueNode<T>> node(TypedNodePath<T> typedNodePath);

    <T> Optional<T> get(TypedNodePath<T> typedNodePath);

    Config edit(Consumer<EditTransaction> transactionConsumer);

    EditResult tryEdit(Consumer<EditTransaction> transaction);
}
