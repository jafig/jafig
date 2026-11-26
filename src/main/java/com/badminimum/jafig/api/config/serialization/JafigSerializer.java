package com.badminimum.jafig.api.config.serialization;

import com.badminimum.jafig.api.config.node.Node;
import com.badminimum.jafig.api.config.node.path.NodePath;
import com.badminimum.jafig.api.config.node.path.StructuralNodePath;
import com.badminimum.jafig.api.config.node.value.ValueNode;
import com.google.common.reflect.TypeToken;
import org.jspecify.annotations.NullMarked;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/// Serializers are ran once the full config is loaded.
/// Two ways it can be done:
/// 1. The loaded config is "compared" with the expected config, when an StructuralNodePath is found where a
/// TypedNodePath was expected it tries to find a serializer.
/// 2. Get all nodes from the config, filter out all non-value nodes, then get their path run a serializer
@NullMarked
public interface JafigSerializer<T> {
    TypeToken<T> type();

    /// Intended for checking if the value can be used for serialization (examples of unusable values could be missing
    /// values, being null)
    boolean canSerialize(T value);

    /// Intended for checking if a node path can be used for deserialization (examples of unusable node paths ar
    /// missing leafs)
    /// Using NodePath since the Serializer can write one value instead of creating a new tree.
    boolean canDeserialize(Reader reader);

    /// Only executes when `canBeConstructed` is true
    /// You can use relative NodePaths with Reader#find and Reader#require.
    T deserialize(Reader reader);

    /// Only executes when `canBeConstructed` is true
    /// Return type mimics the flatmap from Config, returns a map of all nodes with their node paths.
    /// Once all serializers ran this is merged with the result of all other serializers and then with the config's
    /// flatmap.
    void serialize(Writer writer, T value);

    /// Reads the nodes for a serializer, root is the node path from the ValueNode.
    /// With that I mean that a ValueNode is expected, but the file on disk has it as a object/list node. The key
    /// that the ValueNode would have is the root.
    /// Can't go further up than its own root.
    /// Example:
    /// ```yaml
    /// database: # <- This is the root, for the api it is an ValueNode<DatabaseConfig>
    ///   host: ...
    ///   port: ...
    /// ```
    /// In the example above this reader can go to database.host and database.port but to no other node.
    /// Relative NodePaths are supported since the root ist he ValueNode<?>'s path.
    interface Reader {
        NodePath root();

        Node require(StructuralNodePath structuralNodePath);

        Optional<Node> find(StructuralNodePath structuralNodePath);

        <T> ValueNode<T> require(StructuralNodePath structuralNodePath, TypeToken<T> typeToken);

        <T> Optional<ValueNode<T>> find(StructuralNodePath structuralNodePath, TypeToken<T> typeToken);

        /// Empty when it hits a leaf. (ValueNode)
        /// Contains the keys from {@link Reader#immediateChildren(StructuralNodePath)}
        Set<String> immediateChildKeys(StructuralNodePath structuralNodePath);

        Stream<Node> immediateChildren(StructuralNodePath structuralNodePath);
    }

    /// Writing at a path not prefixed by {@link root()} is a contract violation, behavior is set by Config.
    interface Writer {
        void node(NodePath path, Node node);

        /// Same as the example from {@link Reader}'s docs. Is the ValueNode<?>'s path.
        NodePath root();

        /// Returns the nodes added through {@link Writer#node(NodePath, Node)} as a map, can then be read using
        ///  {@link Reader}
        Map<NodePath, Node> toMap();
    }
}