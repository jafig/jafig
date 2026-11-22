package com.badminimum.jafig.api.config.edit;

import com.badminimum.jafig.api.config.node.Node;
import com.badminimum.jafig.api.config.node.path.NodePath;
import com.badminimum.jafig.api.config.node.path.TypedNodePath;
import com.badminimum.jafig.api.config.node.value.ValueNode;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

@NullMarked
public interface EditTransaction {
    <T> EditTransaction set(TypedNodePath<T> path, @Nullable T value);

    default <T> EditTransaction set(ValueNode<T> node, @Nullable T value) {
        return this.set(node.path(), value);
    }

    EditTransaction put(Node node);

    EditTransaction remove(NodePath path);

    default EditTransaction remove(Node node) {
        return this.remove(node.path());
    }

    EditTransaction move(NodePath from, NodePath to);

    default EditTransaction move(Node from, Node to) {
        return this.move(from.path(), to.path());
    }

    <T> EditTransaction update(TypedNodePath<T> path, Function<@Nullable T, @Nullable T> updateFunction);

    default <T> EditTransaction update(ValueNode<T> node, Function<@Nullable T, @Nullable T> updateFunction) {
        return this.update(node.path(), updateFunction);
    }
}
