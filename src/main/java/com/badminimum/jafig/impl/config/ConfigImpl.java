package com.badminimum.jafig.impl.config;

import com.badminimum.jafig.api.config.Config;
import com.badminimum.jafig.api.config.edit.EditResult;
import com.badminimum.jafig.api.config.edit.EditTransaction;
import com.badminimum.jafig.api.config.exception.TypeException;
import com.badminimum.jafig.api.config.node.Node;
import com.badminimum.jafig.api.config.node.path.NodePath;
import com.badminimum.jafig.api.config.node.path.TypedNodePath;
import com.badminimum.jafig.api.config.node.value.ValueNode;
import com.google.common.collect.ImmutableMap;

import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("ClassCanBeRecord")
public class ConfigImpl implements Config {
    private final ImmutableMap<NodePath, Node> nodes;

    public ConfigImpl(ImmutableMap<NodePath, Node> nodes) {
        this.nodes = nodes;
    }

    @Override
    public Optional<Node> node(NodePath nodePath) {
        final Node node = this.nodes.get(nodePath);
        return Optional.ofNullable(node);
    }

    @Override
    public ImmutableMap<NodePath, Node> nodes() {
        return this.nodes;
    }

    @Override
    public <T> Optional<ValueNode<T>> node(TypedNodePath<T> typedNodePath) {
        return this.node((NodePath) typedNodePath).map((Node node) -> {
            if (!(node instanceof ValueNode<?> value)) {
                throw TypeException.NOT_RIGHT_NODE_KIND(ValueNode.class, node.getClass());
            }
            if (!value.path().typeToken().equals(typedNodePath.typeToken())) {
                throw TypeException.TYPES_DO_NOT_MATCH(typedNodePath.typeToken(), value.path().typeToken());
            }
            //noinspection unchecked
            return (ValueNode<T>) value;
        });
    }

    @Override
    public <T> Optional<T> get(TypedNodePath<T> typedNodePath) {
        return this.node(typedNodePath).flatMap(ValueNode::valueOrDefault);
    }

    @Override
    public Config edit(Consumer<EditTransaction> transactionConsumer) {
        return null;
    }

    @Override
    public EditResult tryEdit(Consumer<EditTransaction> transaction) {
        return null;
    }
}
