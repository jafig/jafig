package com.pixelatedslice.easyconfig.api.serialization;

import com.google.common.reflect.TypeToken;
import org.jspecify.annotations.NullMarked;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

@NullMarked
public enum SerializerType {
    FORMAT,
    NODE,
}
package com.pixelatedslice.easyconfig.api.serialization;

import com.pixelatedslice.easyconfig.api.exception.DuplicateException;
import org.jspecify.annotations.NullMarked;

import java.util.function.BinaryOperator;

@NullMarked
public enum NodeType {
    PLAIN_NODE(false),
    CONTAINER_NODE(false),
    COLLECTION_NODE(false),
    VALUE_NODE(true),
    ENV_NODE(true);

    private final boolean isValueBased;

    NodeType(boolean isValueBased) {
        this.isValueBased = isValueBased;
    }

    public static String[] valueNodeBased() {
        return Stream.of(values()).filter(NodeType::isValueBased).map(Enum::toString).toArray(String[]::new);
    }

    public boolean isValueBased() {
        return this.isValueBased;
    }
}
package com.pixelatedslice.easyconfig.api.serialization;

import com.pixelatedslice.easyconfig.api.config.node.Node;
import com.pixelatedslice.easyconfig.api.config.node.factory.builder.NodeBuilderGroupStep;
import org.jspecify.annotations.NullMarked;

@SuppressWarnings("unused")
@NullMarked
public interface SerializerRegistry {
    static SerializerRegistry global() {
        return SerializerRegistryHidden.GLOBAL;
    }

    SerializerRegistry createChild();

    Optional<SerializerRegistry> parent();

    Stream<Serializer<?>> stream();

    <T> Optional<Serializer<T>> serializerFor(TypeToken<T> token);

    default <T> Optional<Serializer<T>> serializerFor(Class<T> token) {
        return this.serializerFor(TypeToken.of(token));
    }

    SerializerRegistry register(Consumer<SerializerRegistryOptions> options, Iterator<Serializer<?>> serializers);

    default SerializerRegistry register(Iterator<Serializer<?>> serializers) {
        return this.register(_ -> {
        }, serializers);
    }

    default SerializerRegistry register(Consumer<SerializerRegistryOptions> options,
            Iterable<Serializer<?>> serializers) {
        return this.register(options, serializers.iterator());
    }

    default SerializerRegistry register(Iterable<Serializer<?>> serializers) {
        return this.register(_ -> {
        }, serializers.iterator());
    }

    default SerializerRegistry register(Consumer<SerializerRegistryOptions> options, Serializer<?>... serializers) {
        return this.register(options, List.of(serializers));
    }

    @SuppressWarnings("UnusedReturnValue")
    default SerializerRegistry register(Serializer<?>... serializers) {
        return this.register(_ -> {
        }, serializers);
    }


}
package com.pixelatedslice.easyconfig.api.serialization;

import com.pixelatedslice.easyconfig.api.config.node.Node;
import com.pixelatedslice.easyconfig.api.exception.SerializeException;
import org.jspecify.annotations.NullMarked;

@SuppressWarnings("unused")
@NullMarked
public interface SerializerRegistryOptions {


    @SuppressWarnings("UnusedReturnValue")
    SerializerRegistryOptions duplicateRegisterStyle(DuplicateRegisterStyle style);

    SerializerRegistryOptions.DuplicateRegisterStyle duplicateRegisterStyle();

    enum DuplicateRegisterStyle {
        THROW((first, replacement) -> {
            throw new DuplicateException(first, replacement);
        }),
        KEEP_ORIGINAL((first, _) -> first),
        REPLACE((_, replacement) -> replacement);

        private final BinaryOperator<Serializer<?>> action;

        DuplicateRegisterStyle(BinaryOperator<Serializer<?>> action) {
            this.action = action;
        }

        public BinaryOperator<Serializer<?>> action() {
            return this.action;
        }

        public <T> Serializer<T> apply(Serializer<T> original, Serializer<T> replacement) {
            //noinspection unchecked
            return (Serializer<T>) this.action.apply(original, replacement);
        }
    }
}
package com.pixelatedslice.easyconfig.api.serialization;

import org.jspecify.annotations.NullMarked;


@SuppressWarnings("unused")
@FunctionalInterface
@NullMarked
public interface Deserialize<T> {
    Node deserialize(T value, NodeBuilderGroupStep.Container builder, SerializeContext context);
}
package com.pixelatedslice.easyconfig.api.serialization;

import com.google.common.reflect.TypeToken;
import com.pixelatedslice.easyconfig.api.config.node.Node;
import com.pixelatedslice.easyconfig.api.config.node.factory.builder.NodeBuilderGroupStep;
import org.jspecify.annotations.NullMarked;

@SuppressWarnings("unused")
@FunctionalInterface
@NullMarked
public interface Serialize<T> {
    T serialize(Node rootNode, SerializeContext context) throws SerializeException;
}
package com.pixelatedslice.easyconfig.api.serialization;

import org.jspecify.annotations.NullMarked;


import java.util.ServiceLoader;

@NullMarked
public interface SerializeContext {
}
package com.pixelatedslice.easyconfig.api.serialization;

import org.jspecify.annotations.NullMarked;


@SuppressWarnings("unused")
@NullMarked
public interface Serializer<T> extends Serialize<T>, Deserialize<T> {
    TypeToken<T> type();

    Node buildStructure(NodeBuilderGroupStep.Container builder);
}
package com.pixelatedslice.easyconfig.api.utils.primitive;

import org.jspecify.annotations.NullMarked;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("unused")
@NullMarked
public interface Format {
    String[] fileExtensions();

    default String preferredFileExtension() {
        return this.fileExtensions()[0];
    }

    void write(BuiltConfig config, Writer writer);

    default String writeString(BuiltConfig config) {
        final var writer = new StringWriter();
        this.write(config, writer);
        return writer.toString();
    }

    default void writeToFile(BuiltConfig config, File file) throws IOException {
        this.write(config, new FileWriter(file));
    }

    default void writeToFile(BuiltConfig config, Path path) throws IOException {
        this.writeToFile(config, path.toFile());
    }

    Config read(ConfigStructure structure, Reader reader);

    default Config readFile(ConfigStructure structure, File file) throws FileNotFoundException {
        return this.read(structure, new FileReader(file));
    }

    default Config readFile(ConfigStructure structure, Path path) throws FileNotFoundException {
        return this.readFile(structure, path.toFile());
    }

    default Config readString(ConfigStructure structure, String rawString) {
        final var reader = new StringReader(rawString);
        return this.read(structure, reader);
    }
}
package com.pixelatedslice.easyconfig.api.utils.typetoken;

import com.google.common.reflect.TypeToken;
import com.pixelatedslice.easyconfig.api.exception.TypeException;
import org.jspecify.annotations.NullMarked;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@FunctionalInterface
@NullMarked
public interface ValidateOption<Value> {

    Optional<Value> onValidationError(@Nullable Value value, ValidatorContext context);

}
package com.pixelatedslice.easyconfig.api.utils.typetoken;

import com.google.common.reflect.TypeToken;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings({"unused", "LambdaUnfriendlyMethodOverload"})
@FunctionalInterface
@NullMarked
public interface Validator<T extends @Nullable Object> {
    String DEFAULT_ERROR_MESSAGE = "Invalid value: \"%s\". The input does not meet the requirements of the predicate.";

    static <T> Validator<T> empty() {
        return (@Nullable T _, ValidatorContext _) -> {
        };
    }

    static <T> void validate(T value, Predicate<? super T> predicate, ValidatorContext context) {
        if (!predicate.test(value)) {
            context.error(DEFAULT_ERROR_MESSAGE, value);
        }
    }

    static void validate(int value, IntPredicate predicate, ValidatorContext context) {
        if (!predicate.test(value)) {
            context.error(DEFAULT_ERROR_MESSAGE, value);
        }
    }

    static void validate(long value, LongPredicate predicate, ValidatorContext context) {
        if (!predicate.test(value)) {
            context.error(DEFAULT_ERROR_MESSAGE, value);
        }
    }

    static void validate(double value, DoublePredicate predicate, ValidatorContext context) {
        if (!predicate.test(value)) {
            context.error(DEFAULT_ERROR_MESSAGE, value);
        }
    }

    void validate(@Nullable T value, ValidatorContext context);
}
package com.pixelatedslice.easyconfig.api.format;

import com.pixelatedslice.easyconfig.api.config.BuiltConfig;
import com.pixelatedslice.easyconfig.api.config.Config;
import com.pixelatedslice.easyconfig.api.config.ConfigStructure;
import org.jspecify.annotations.NullMarked;

import java.io .*;
        import java.nio.file.Path;

@NullMarked
public interface ValidatorContext {
    void error(String message, Object... variables);

    boolean hasError();

    Collection<String> errors();
}
package com.pixelatedslice.easyconfig.api.format;

import org.jspecify.annotations.NullMarked;


import java.util.Collection;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

@FunctionalInterface
public interface NullPolicy {
    void handle(@Nullable Object any);
}
package com.pixelatedslice.easyconfig.api.validator.option;

import com.pixelatedslice.easyconfig.api.validator.ValidatorContext;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public interface NullPolicies {
    @SuppressWarnings("unused")
    NullPolicy IGNORE = (@Nullable Object _) -> {
    };
    NullPolicy THROW = (@Nullable Object any) -> {
        if (any != null) {
            return;
        }
        throw new IllegalArgumentException("This method does not accept null args.");
    };
}
package com.pixelatedslice.easyconfig.api.validator.option;

import com.pixelatedslice.easyconfig.api.exception.ValidationException;
import com.pixelatedslice.easyconfig.api.validator.ValidatorContext;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

@SuppressWarnings("unused")
@NullMarked
public interface BuiltConfig extends Config {
    SerializerRegistry serializers();
}
package com.pixelatedslice.easyconfig.api.validator;

import org.jspecify.annotations.NullMarked;

import org.jspecify.annotations.Nullable;

import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

@NullMarked
public interface CollectionNode extends Node {
    @Override
    default NodeType nodeType() {
        return NodeType.COLLECTION_NODE;
    }


    @CheckReturnValue
    ImmutableCollection<ReturnedNode> nodes();


    @CheckReturnValue
    Stream<ReturnedNode> stream();


    @CheckReturnValue
    ReturnedNode atIndex(int index);

    @Override
    NodeBuilderKeySteps.Collection toBuilder();
}

package com.pixelatedslice.easyconfig.api.validator;

import org.jspecify.annotations.NullMarked;


import java.util.Collection;

@SuppressWarnings("unused")
@NullMarked
public interface ContainerNode extends Node, Editable<EditableContainerNode> {
    default NodeType nodeType() {
        return NodeType.CONTAINER_NODE;
    }

    @Override
    NodeBuilderKeySteps.Container toBuilder();

    ImmutableList<Node> children();

    boolean isRootNode();

    ReturnedNode node(String... path);

    default Optional<ContainerNode> containerNode(String... path) {
        return this.node(path).container();
    }

    default <T> Optional<ValueNode<T>> valueNode(Class<T> simpleType, String... path) {
        Objects.requireNonNull(simpleType);
        Objects.requireNonNull(path);

        return this.valueNode(TypeTokenUtils.getSimpleOrThrow(simpleType), path);
    }

    default <T> Optional<ValueNode<T>> valueNode(TypeToken<T> typeToken, String... path) {
        Objects.requireNonNull(typeToken);
        Objects.requireNonNull(path);

        return this.node(path).value(typeToken);
    }

    default <T> Optional<EnvNode<T>> envNode(Class<T> simpleType, String... path) {
        Objects.requireNonNull(simpleType);
        Objects.requireNonNull(path);

        return this.envNode(TypeTokenUtils.getSimpleOrThrow(simpleType), path);
    }

    default <T> Optional<EnvNode<T>> envNode(TypeToken<T> typeToken, String... path) {
        Objects.requireNonNull(typeToken);
        Objects.requireNonNull(path);

        return this.node(path).env(typeToken);
    }
}
package com.pixelatedslice.easyconfig.api.validator.null_policy;

import org.jspecify.annotations.Nullable;

@SuppressWarnings({"unused", "UnusedReturnValue"})
@NullMarked
public interface EditableContainerNode extends EditableVariant {
    default EditableContainerNode addNodes(Node... nodes) {
        return this.addNodes(List.of(nodes));
    }

    EditableContainerNode addNodes(Collection<? extends Node> nodes);

    EditableContainerNode setNodes(Collection<? extends Node> nodes);

    default EditableContainerNode removeNodes(Node... nodes) {
        return this.removeNodes(List.of(nodes));
    }

    EditableContainerNode removeNodes(Collection<? extends Node> nodes);

    EditableContainerNode removeNodes(String... keys);

    EditableContainerNode clearNodes();
}
package com.pixelatedslice.easyconfig.api.validator.null_policy;

import org.jspecify.annotations.Nullable;

@FunctionalInterface
@NullMarked
public interface EnvAdapter<T> extends Function<String, @Nullable T> {


}
package com.pixelatedslice.easyconfig.api.exception;

import com.google.common.reflect.TypeToken;
import com.pixelatedslice.easyconfig.api.config.node.NodeType;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public interface EnvNode<T> extends Node {
    default NodeType nodeType() {
        return NodeType.ENV_NODE;
    }

    String envKey();

    default Optional<T> value() {
        return this.value(ValidationOptions.throwExceptions());
    }

    Optional<T> value(ValidateOption<T> validateOption);


    Function<String, @Nullable T> adapter();

    Validator<T> validator();

    TypeToken<T> typeToken();

    @Override
    NodeBuilderKeySteps.Env<T> toBuilder();
}
package com.pixelatedslice.easyconfig.api.exception;

import com.google.common.reflect.TypeToken;
import com.pixelatedslice.easyconfig.api.serialization.SerializerType;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public interface EditableValueNode<T> extends EditableVariant {
    EditableValueNode<T> setValue(@Nullable T value);
}
package com.pixelatedslice.easyconfig.api.exception;

import com.google.common.reflect.TypeToken;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public interface ValueNode<T> extends Node, Editable<EditableValueNode<T>> {
    @Override
    NodeBuilderKeySteps.Value<T> toBuilder();

    default NodeType nodeType() {
        return NodeType.VALUE_NODE;
    }

    default Optional<T> value() {
        return this.value(ValidationOptions.throwExceptions());
    }

    Optional<T> value(ValidateOption<T> option);

    default Optional<T> defaultValue() {
        return this.defaultValue(ValidationOptions.throwExceptions());
    }

    Optional<T> defaultValue(ValidateOption<T> option);

    default Optional<T> valueOrDefault() {
        return this.valueOrDefault(ValidationOptions.throwExceptions());
    }

    Optional<T> valueOrDefault(ValidateOption<T> option);

    Optional<Serializer<T>> serializer();

    Validator<T> validator();

    TypeToken<T> typeToken();
}
package com.pixelatedslice.easyconfig.api.exception;

import com.pixelatedslice.easyconfig.api.validator.ValidatorContext;
import org.jspecify.annotations.NullMarked;

@SuppressWarnings("unused")
public sealed interface NodeIterator<N extends Node> extends Iterator<N>
        permits NodeIterator.Container, NodeIterator.Env, NodeIterator.Value {
    non-sealed interface Container extends NodeIterator<ContainerNode> {
        static Optional<ContainerNode> find(Collection<? extends Node> nestedNodes, String... providedKeys) {
            Objects.requireNonNull(nestedNodes);
            Objects.requireNonNull(providedKeys);

            if (providedKeys.length == 0) {
                return Optional.empty();
            }

            final List<String> keys = List.of(providedKeys);
            var currentNestedNodes = nestedNodes;
            final int last = keys.size() - 1;
            for (int i = 0; i <= last; i++) {
                final String wanted = keys.get(i);
                ContainerNode next = null;
                for (Node node : currentNestedNodes) {
                    if (!(node instanceof ContainerNode containerNode)) {
                        continue;
                    }

                    if (containerNode.key().equals(wanted)) {
                        next = containerNode;
                        break;
                    }
                }
                if (next == null) {
                    return Optional.empty();
                }
                if (i == last) {
                    return Optional.of(next);
                }
                currentNestedNodes = next.children();
            }
            return Optional.empty();
        }
    }

    non-sealed interface Value extends NodeIterator<ValueNode<?>> {
        static <T> Optional<ValueNode<T>> find(ContainerNode rootContainer, TypeToken<T> typeToken,
                String... providedKeys) {
            Objects.requireNonNull(rootContainer);
            Objects.requireNonNull(typeToken);
            Objects.requireNonNull(providedKeys);

            if (providedKeys.length == 0) {
                return Optional.empty();
            }

            if (providedKeys.length == 1) {
                final var nodeKey = providedKeys[0];
                for (var node : rootContainer.children()) {
                    if (!(node instanceof ValueNode<?> valueNode)) {
                        continue;
                    }

                    if (!valueNode.key().equals(nodeKey)) {
                        continue;
                    }

                    if (!TypeTokenUtils.matches(valueNode.typeToken(), typeToken)) {
                        throw NodeException.NODE_HAS_WRONG_VALUE_TYPE(nodeKey,
                                valueNode.typeToken(),
                                typeToken.toString());
                    }

                    @SuppressWarnings("unchecked")
                    final var casted = (ValueNode<T>) valueNode;
                    return Optional.of(casted);
                }
                return Optional.empty();
            }
            final String[] parentKeys = Arrays.copyOf(providedKeys, providedKeys.length - 1);
            final var nodeKey = providedKeys[providedKeys.length - 1];
            final var sectionOptional = Container.find(rootContainer.children(), parentKeys);
            return sectionOptional.flatMap((ContainerNode section) -> section.valueNode(typeToken, nodeKey));
        }
    }

    non-sealed interface Env extends NodeIterator<EnvNode<?>> {
        @SuppressWarnings("unchecked")
        static <T> Optional<EnvNode<T>> find(ContainerNode rootContainer, TypeToken<T> typeToken,
                String... providedKeys) {
            final var opt = Value.find(rootContainer, typeToken, providedKeys);

            if (opt.isEmpty()) {
                return Optional.empty();
            }

            final var found = opt.get();
            if (!(found instanceof EnvNode<?>)) {
                throw NodeException.DID_NOT_EXPECT_NODE_TYPE(found.key(), NodeType.ENV_NODE, found.nodeType());
            }
            return Optional.of((EnvNode<T>) found);
        }
    }
}
package com.pixelatedslice.easyconfig.api.exception;

import org.jspecify.annotations.NullMarked;


@SuppressWarnings("unused")
@NullMarked
public interface ReturnedNode {
    boolean hasNode();

    Optional<String> nodeKey();

    Optional<Node> plainNode();

    Optional<ContainerNode> container();

    Optional<CollectionNode> collectionNode();

    <T> Optional<ValueNode<T>> value(Class<T> simpleType);

    <T> Optional<ValueNode<T>> value(TypeToken<T> typeToken);

    Optional<ValueNode<?>> unsafeValue();

    <T> Optional<EnvNode<T>> env(Class<T> simpleType);

    <T> Optional<EnvNode<T>> env(TypeToken<T> typeToken);

    Optional<EnvNode<?>> unsafeEnv();
}
package com.pixelatedslice.easyconfig.api.exception;

import org.jspecify.annotations.NullMarked;


@SuppressWarnings("unused")
@NullMarked
public interface NodeBuilder {
    @NullMarked
    interface NodeTypeStep extends NodeBuilder {
        <T> NodeBuilderKeySteps.Value<T> value();

        <T> NodeBuilderKeySteps.Env<T> env();

        NodeBuilderKeySteps.Container container();

        NodeBuilderKeySteps.Collection collection();
    }

    @NullMarked
    interface KeyStep<Next extends NodeBuilder> extends NodeBuilder {
        Next key(String key);
    }

    @NullMarked
    interface BuildStep<NodeType extends Node> extends NodeBuilder {
        NodeType build();
    }
}
package com.pixelatedslice.easyconfig.api.config;

import com.pixelatedslice.easyconfig.api.serialization.SerializerRegistry;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface NodeBuilderEnvStep {
    @NullMarked
    interface VariableStep<T> extends NodeBuilder {
        AdapterValidatorStep<T> variable(String variable);
    }

    @NullMarked
    interface AdapterValidatorStep<T> extends NodeBuilder.BuildStep<EnvNode<T>> {
        AdapterValidatorStep<T> adapter(@Nullable Function<String, @Nullable T> adapter);

        AdapterValidatorStep<T> validator(@Nullable Validator<T> validator);
    }

    @NullMarked
    interface PreDefined<T extends @Nullable Object> {
        String key();

        Function<String, @Nullable T> adapter();
    }
}
package com.pixelatedslice.easyconfig.api.config.node.collection;

import com.google.common.collect.ImmutableCollection;
import com.google.errorprone.annotations.CheckReturnValue;
import com.pixelatedslice.easyconfig.api.config.node.Node;
import com.pixelatedslice.easyconfig.api.config.node.NodeType;
import com.pixelatedslice.easyconfig.api.config.node.ReturnedNode;
import com.pixelatedslice.easyconfig.api.config.node.factory.builder.NodeBuilderKeySteps;
import org.jspecify.annotations.NullMarked;

import java.util.stream.Stream;

@SuppressWarnings("unused")
@NullMarked
public interface NodeBuilderValueStep<T> extends NodeBuilder, NodeBuilder.BuildStep<ValueNode<T>> {
    NodeBuilderValueStep<T> value(@Nullable T value);

    NodeBuilderValueStep<T> defaultValue(@Nullable T defaultValue);

    NodeBuilderValueStep<T> validator(@Nullable Validator<T> validator);

    NodeBuilderValueStep<T> serializer(@Nullable Serializer<T> serializer);
}
package com.pixelatedslice.easyconfig.api.config.node.container;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.pixelatedslice.easyconfig.api.config.node.Node;
import com.pixelatedslice.easyconfig.api.config.node.NodeType;
import com.pixelatedslice.easyconfig.api.config.node.ReturnedNode;
import com.pixelatedslice.easyconfig.api.config.node.env.EnvNode;
import com.pixelatedslice.easyconfig.api.config.node.factory.builder.NodeBuilderKeySteps;
import com.pixelatedslice.easyconfig.api.config.node.value.ValueNode;
import com.pixelatedslice.easyconfig.api.editable.Editable;
import com.pixelatedslice.easyconfig.api.utils.typetoken.TypeTokenUtils;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("unused")
@NullMarked
public interface NodeBuilderKeySteps {
    @NullMarked
    interface Value<T> extends NodeBuilderKeySteps, NodeBuilder.KeyStep<NodeBuilderValueStep<T>> {
    }

    @NullMarked
    interface Env<T> extends NodeBuilderKeySteps, NodeBuilder.KeyStep<NodeBuilderEnvStep.VariableStep<T>> {
    }

    @NullMarked
    interface Container extends NodeBuilderKeySteps, NodeBuilder.KeyStep<NodeBuilderGroupStep.Container> {
    }

    @NullMarked
    interface Collection extends NodeBuilderKeySteps, NodeBuilder.KeyStep<NodeBuilderGroupStep.Collection> {
    }
}
package com.pixelatedslice.easyconfig.api.config.node.container;

import com.pixelatedslice.easyconfig.api.config.node.Node;
import com.pixelatedslice.easyconfig.api.editable.EditableVariant;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
@NullMarked
public interface NodeBuilderGroupStep<Next extends NodeBuilder, NodeType extends Node>
        extends NodeBuilder, NodeBuilder.BuildStep<NodeType> {
    Next children(NullPolicy nullPolicy, @Nullable BuildStep<?> @Nullable ... nodes);

    default Next children(@Nullable BuildStep<?> @Nullable ... nodes) {
        return this.children(NullPolicies.THROW, nodes);
    }

    Next children(NullPolicy nullPolicy, java.util.@Nullable Collection<? extends @Nullable BuildStep<?>> nodes);

    default Next children(java.util.@Nullable Collection<? extends @Nullable BuildStep<?>> nodes) {
        return this.children(NullPolicies.THROW, nodes);
    }

    Next builtChildren(NullPolicy nullPolicy, @Nullable Node @Nullable ... nodes);

    default Next builtChildren(@Nullable Node @Nullable ... nodes) {
        return this.builtChildren(NullPolicies.THROW, nodes);
    }

    Next builtChildren(NullPolicy nullPolicy, java.util.@Nullable Collection<? extends Node> nodes);

    default Next builtChildren(java.util.@Nullable Collection<? extends Node> nodes) {
        return this.builtChildren(NullPolicies.THROW, nodes);
    }

    @NullMarked
    interface Container extends NodeBuilderGroupStep<Container, ContainerNode> {
    }

    @NullMarked
    interface Collection extends NodeBuilderGroupStep<Collection, CollectionNode> {
    }
}
package com.pixelatedslice.easyconfig.api.config.node.env;

import org.jspecify.annotations.NullMarked;

import org.jspecify.annotations.Nullable;

import java.util.function.Function;

@SuppressWarnings("unused")
@NullMarked
public interface Nodes {
    static <T> NodeBuilderKeySteps.Value<T> value(Class<T> simpleType) {
        return Nodes.value(TypeTokenUtils.getSimpleOrThrow(simpleType));
    }

    static <T> ValueNode<T> emptyValue(Class<T> simpleType, String key) {
        return Nodes.emptyValue(TypeTokenUtils.getSimpleOrThrow(simpleType), key);
    }

    static <T> NodeBuilderKeySteps.Value<T> value(TypeToken<T> typeToken) {
        return NodeFactoryService.INSTANCE.createValueNodeBuilder(typeToken);
    }

    static <T> ValueNode<T> emptyValue(TypeToken<T> typeToken, String key) {
        return Nodes.value(typeToken, key, null, null);
    }

    static <T> ValueNode<T> value(Class<T> simpleType, String key, @Nullable T defaultValue) {
        return Nodes.value(TypeTokenUtils.getSimpleOrThrow(simpleType), key, defaultValue);
    }

    static <T> ValueNode<T> value(Class<T> simpleType, String key, @Nullable T defaultValue, @Nullable T value) {
        return Nodes.value(TypeTokenUtils.getSimpleOrThrow(simpleType), key, defaultValue, value);
    }

    static <T> ValueNode<T> value(TypeToken<T> typeToken, String key, @Nullable T defaultValue) {
        return NodeFactoryService.INSTANCE.createValueNode(typeToken, key, defaultValue, null);
    }

    static <T> ValueNode<T> value(TypeToken<T> typeToken, String key, @Nullable T defaultValue, @Nullable T value) {
        return NodeFactoryService.INSTANCE.createValueNode(typeToken, key, defaultValue, value);
    }

    static <T> NodeBuilderKeySteps.Env<T> env(Class<T> simpleType) {
        return Nodes.env(TypeTokenUtils.getSimpleOrThrow(simpleType));
    }

    static <T> NodeBuilderKeySteps.Env<T> env(TypeToken<T> typeToken) {
        return NodeFactoryService.INSTANCE.createEnvNodeBuilder(typeToken);
    }

    static <T> EnvNode<T> env(Class<T> simpleType, String key, String variable) {
        return Nodes.env(TypeTokenUtils.getSimpleOrThrow(simpleType), key, variable);
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    static <T> EnvNode<T> env(Class<T> simpleType, String key, String variable,
            @Nullable Function<String, @Nullable T> adapter, @Nullable Validator<T> validator) {
        return Nodes.env(TypeTokenUtils.getSimpleOrThrow(simpleType), key, variable, adapter, validator);
    }

    static <T> EnvNode<T> env(TypeToken<T> typeToken, String key, String variable) {
        return NodeFactoryService.INSTANCE.createEnvNode(typeToken, key, variable, null, null);
    }

    @SuppressWarnings("MethodWithTooManyParameters")
    static <T> EnvNode<T> env(TypeToken<T> typeToken, String key, String variable,
            @Nullable Function<String, @Nullable T> adapter, @Nullable Validator<T> validator) {
        return NodeFactoryService.INSTANCE.createEnvNode(typeToken, key, variable, adapter, validator);
    }

    static <T> NodeBuilderEnvStep.AdapterValidatorStep<T> envBuilder(Class<T> simpleType, String key, String variable) {
        return Nodes.envBuilder(TypeTokenUtils.getSimpleOrThrow(simpleType), key, variable);
    }

    static <T> NodeBuilderEnvStep.AdapterValidatorStep<T> envBuilder(TypeToken<T> typeToken, String key,
            String variable) {
        return NodeFactoryService.INSTANCE.createEnvNodeBuilder(typeToken, key, variable);
    }

    static NodeBuilderGroupStep.Container container(String key) {
        return NodeFactoryService.INSTANCE.createContainerNodeBuilder(key);
    }

    static NodeBuilderGroupStep.Collection collection(String key) {
        return NodeFactoryService.INSTANCE.createCollectionNodeBuilder(key);
    }
}
package com.pixelatedslice.easyconfig.api.config.node.env;

import com.pixelatedslice.easyconfig.api.config.node.factory.builder.NodeBuilderEnvStep;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

@SuppressWarnings("unused")
@NullMarked
public interface CommonTypes {
    TypeToken<BigDecimal> BIG_DECIMAL = TypeToken.of(BigDecimal.class);
    TypeToken<BigInteger> BIG_INTEGER = TypeToken.of(BigInteger.class);
    TypeToken<Boolean> BOOLEAN = TypeToken.of(boolean.class);
    TypeToken<Byte> BYTE = TypeToken.of(byte.class);
    TypeToken<Character> CHARACTER = TypeToken.of(char.class);
    TypeToken<Double> DOUBLE = TypeToken.of(double.class);
    TypeToken<Duration> DURATION = TypeToken.of(Duration.class);
    TypeToken<Float> FLOAT = TypeToken.of(float.class);
    TypeToken<Integer> INTEGER = TypeToken.of(int.class);
    TypeToken<Long> LONG = TypeToken.of(long.class);
    TypeToken<Short> SHORT = TypeToken.of(short.class);
    TypeToken<String> STRING = TypeToken.of(String.class);
    TypeToken<UUID> UUID = TypeToken.of(UUID.class);
}
package com.pixelatedslice.easyconfig.api.config.node.env;

import com.google.common.reflect.TypeToken;
import com.pixelatedslice.easyconfig.api.config.node.Node;
import com.pixelatedslice.easyconfig.api.config.node.NodeType;
import com.pixelatedslice.easyconfig.api.config.node.factory.builder.NodeBuilderKeySteps;
import com.pixelatedslice.easyconfig.api.validator.Validator;
import com.pixelatedslice.easyconfig.api.validator.option.ValidateOption;
import com.pixelatedslice.easyconfig.api.validator.option.ValidationOptions;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

@ApiStatus.OverrideOnly
@NullMarked
public interface NodeFactoryService {
    NodeFactoryService INSTANCE = ServiceLoader
            .load(NodeFactoryService.class)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No NodeBuilders implementation found"));

    <T> NodeBuilderKeySteps.Value<T> createValueNodeBuilder(TypeToken<T> typeToken);

    <T> ValueNode<T> createValueNode(TypeToken<T> type, String key, @Nullable T defaultValue, @Nullable T value);

    <T> NodeBuilderKeySteps.Env<T> createEnvNodeBuilder(TypeToken<T> typeToken);

    @SuppressWarnings("MethodWithTooManyParameters")
    <T> EnvNode<T> createEnvNode(TypeToken<T> typeToken, String key, String variable,
            @Nullable Function<String, @Nullable T> adapter, @Nullable Validator<T> validator);

    <T> NodeBuilderEnvStep.AdapterValidatorStep<T> createEnvNodeBuilder(TypeToken<T> typeToken, String key,
            String variable);

    NodeBuilderGroupStep.Container createContainerNodeBuilder(String key);

    NodeBuilderGroupStep.Collection createCollectionNodeBuilder(String key);
}
package com.pixelatedslice.easyconfig.api.config.node.value;

import com.pixelatedslice.easyconfig.api.editable.EditableVariant;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("unused")
@NullMarked
public interface Node {
    default NodeType nodeType() {
        return NodeType.PLAIN_NODE;
    }

    String key();

    ReturnedNode parent();


    @CheckReturnValue
    NodeBuilder.KeyStep<?> toBuilder();


    @CheckReturnValue
    ConfigStructure toStructure();

    default String[] fullPath() {
        Stream<String> stream = Stream.empty();
        Node current = this;
        while (true) {
            stream = Stream.concat(stream, Stream.of(current.key()));
            if (current.parent().plainNode().isEmpty()) {
                break;
            }
            current = current.parent().plainNode().get();
        }

        final var result = stream.toArray(String[]::new);
        final var reversed = new String[result.length];
        for (int i = 0; i < result.length; i++) {
            reversed[result.length - (1 + i)] = result[i];
        }

        return reversed;
    }
}
package com.pixelatedslice.easyconfig.api.config.node.value;

import com.google.common.reflect.TypeToken;
import com.pixelatedslice.easyconfig.api.config.node.Node;
import com.pixelatedslice.easyconfig.api.config.node.NodeType;
import com.pixelatedslice.easyconfig.api.config.node.factory.builder.NodeBuilderKeySteps;
import com.pixelatedslice.easyconfig.api.editable.Editable;
import com.pixelatedslice.easyconfig.api.serialization.Serializer;
import com.pixelatedslice.easyconfig.api.validator.Validator;
import com.pixelatedslice.easyconfig.api.validator.option.ValidateOption;
import com.pixelatedslice.easyconfig.api.validator.option.ValidationOptions;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

@SuppressWarnings("unused")
@NullMarked
public interface Config {
    Node root();
}
package com.pixelatedslice.easyconfig.api.config.node;

import com.google.common.reflect.TypeToken;
import com.pixelatedslice.easyconfig.api.config.node.container.ContainerNode;
import com.pixelatedslice.easyconfig.api.config.node.env.EnvNode;
import com.pixelatedslice.easyconfig.api.config.node.value.ValueNode;
import com.pixelatedslice.easyconfig.api.exception.NodeException;
import com.pixelatedslice.easyconfig.api.utils.typetoken.TypeTokenUtils;

import java.util .*;

@NullMarked
public interface ConfigStructure extends Config {
}
package com.pixelatedslice.easyconfig.api.config.node;

import org.jspecify.annotations.NullMarked;


import java.util.stream.Stream;

@FunctionalInterface
@NullMarked
public interface Editable<E extends EditableVariant> {
    E editable();
}
package com.pixelatedslice.easyconfig.api.config.node;

import com.google.common.reflect.TypeToken;
import com.pixelatedslice.easyconfig.api.config.node.collection.CollectionNode;
import com.pixelatedslice.easyconfig.api.config.node.container.ContainerNode;
import com.pixelatedslice.easyconfig.api.config.node.env.EnvNode;
import com.pixelatedslice.easyconfig.api.config.node.value.ValueNode;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

@FunctionalInterface
@NullMarked
public interface EditableVariant extends AutoCloseable {
    @Override
    void close();
}
package com.pixelatedslice.easyconfig.api.config.node.factory.builder;

import com.pixelatedslice.easyconfig.api.config.node.Node;
import org.jspecify.annotations.NullMarked;

@NullMarked
final class SerializerRegistryHidden {

    static final SerializerRegistry GLOBAL = ServiceLoader
            .load(SerializerRegistry.class)
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Cannot find SerializerRegistry as a service"));

    private SerializerRegistryHidden() {
    }
}
package com.pixelatedslice.easyconfig.api.config.node.factory.builder;

import com.pixelatedslice.easyconfig.api.config.node.env.EnvNode;
import com.pixelatedslice.easyconfig.api.validator.Validator;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

@SuppressWarnings("unused")
@NullMarked
public final class TypeUtils {
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER = Map.of(boolean.class,
            Boolean.class,
            byte.class,
            Byte.class,
            char.class,
            Character.class,
            double.class,
            Double.class,
            float.class,
            Float.class,
            int.class,
            Integer.class,
            long.class,
            Long.class,
            short.class,
            Short.class,
            void.class,
            Void.class);

    private TypeUtils() {
    }

    public static Class<?> primitiveToWrapper(Class<?> clazz) {
        Objects.requireNonNull(clazz);
        return PRIMITIVE_TO_WRAPPER.getOrDefault(clazz, clazz);
    }

    public static <T> String toString(@Nullable T array) {
        return switch (array) {
            case null -> "null";
            case int[] ints -> Arrays.toString(ints);
            case long[] longs -> Arrays.toString(longs);
            case boolean[] booleans -> Arrays.toString(booleans);
            case byte[] bytes -> Arrays.toString(bytes);
            case char[] chars -> Arrays.toString(chars);
            case float[] floats -> Arrays.toString(floats);
            case double[] doubles -> Arrays.toString(doubles);
            case short[] shorts -> Arrays.toString(shorts);
            case Object[] objects -> Arrays.deepToString(objects);
            default -> String.valueOf(array);
        };
    }
}
package com.pixelatedslice.easyconfig.api.config.node.factory.builder;

import com.pixelatedslice.easyconfig.api.config.node.value.ValueNode;
import com.pixelatedslice.easyconfig.api.serialization.Serializer;
import com.pixelatedslice.easyconfig.api.validator.Validator;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@SuppressWarnings({"unused", "BooleanMethodIsAlwaysInverted"})
@NullMarked
public final class TypeTokenUtils {
    private TypeTokenUtils() {
    }

    public static <T> TypeToken<T> getSimpleOrThrow(Class<T> simpleType) {
        final var typeToken = TypeToken.of(simpleType);

        throwIfNotSimple(simpleType, typeToken);

        return typeToken;
    }

    public static void throwIfNotSimple(Class<?> simpleType, TypeToken<?> typeToken) {
        Objects.requireNonNull(simpleType);
        Objects.requireNonNull(typeToken);

        if (TypeTokenUtils.isSimpleTypeToken(typeToken)) {
            return;
        }

        throw TypeException.CLASS_USED_IN_PLACE_OF_TYPETOKEN(simpleType);
    }

    public static boolean isSimpleTypeToken(TypeToken<?> typeToken) {
        Objects.requireNonNull(typeToken);

        final Type type = typeToken.getType();

        if (!(type instanceof Class<?> clazz)) {
            return false;
        }

        Class<?> leafType = clazz;
        while (leafType.isArray()) {
            leafType = leafType.getComponentType();
        }

        return leafType.getTypeParameters().length == 0;
    }

    public static <T> boolean hasCorrectType(T value, TypeToken<?> typeToken) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(typeToken);

        return TypeTokenTypeComparer.hasCorrectType(value, typeToken);
    }

    public static boolean matches(TypeToken<?> typeTokenOne, TypeToken<?> typeTokenTwo) {
        Objects.requireNonNull(typeTokenOne);
        Objects.requireNonNull(typeTokenTwo);

        return typeTokenOne.equals(typeTokenTwo);
    }

    public static <T> boolean matchingClass(Class<T> targetType, TypeToken<?> typeToken, Class<?> type) {
        Objects.requireNonNull(targetType);
        Objects.requireNonNull(typeToken);
        Objects.requireNonNull(type);

        return matchingClass(targetType, typeToken.getRawType(), type);
    }

    public static <T> boolean matchingClass(Class<T> targetType, Class<?> typeTokenRawType, Class<?> type) {
        Objects.requireNonNull(targetType);
        Objects.requireNonNull(typeTokenRawType);
        Objects.requireNonNull(type);

        return typeTokenRawType.isAssignableFrom(type)
               && targetType.isAssignableFrom(type)
               && targetType.isAssignableFrom(typeTokenRawType);
    }

    public static List<TypeToken<?>> generics(TypeToken<?> typeToken) {
        Objects.requireNonNull(typeToken);

        final List<TypeToken<?>> generics = new ArrayList<>();
        for (var typeParameter : typeToken.getRawType().getTypeParameters()) {
            generics.add(typeToken.resolveType(typeParameter));
        }
        return Collections.unmodifiableList(generics);
    }
}
package com.pixelatedslice.easyconfig.api.config.node.factory.builder;

import org.jspecify.annotations.NullMarked;

@SuppressWarnings("unused")

@NullMarked
final class TypeTokenTypeComparer {
    private TypeTokenTypeComparer() {
    }

    static <T> boolean hasCorrectType(T value, TypeToken<?> typeToken) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(typeToken);

        final var baseType = typeToken.getRawType();

        if (!baseType.isInstance(value)) {
            return false;
        }

        final var valueClass = value.getClass();

        if (TypeTokenUtils.matchingClass(Collection.class, baseType, valueClass)) {
            return iterable(value, typeToken);
        } else if (valueClass.isArray() && baseType.equals(valueClass)) {
            return iterable(value, typeToken);
        } else if (TypeTokenUtils.matchingClass(Map.class, baseType, valueClass)) {
            return map((Map<?, ?>) value, typeToken);
        }

        return typeToken.getRawType().isInstance(value);
    }

    private static boolean iterable(Object container, TypeToken<?> typeToken) {
        throw new RuntimeException();
    }

    private static boolean map(Map<?, ?> map, TypeToken<?> typeToken) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(typeToken);

        if (map.isEmpty()) {
            return true;
        }

        final var generics = TypeTokenUtils.generics(typeToken);
        final var keyGeneric = generics.get(0);
        final var keyGenericClass = keyGeneric.getRawType();
        final var valueGeneric = generics.get(1);
        final var valueGenericClass = valueGeneric.getRawType();

        for (var entry : map.entrySet()) {
            final var key = entry.getKey();
            final var value = entry.getValue();

            if (!keyGenericClass.isInstance(key) || !valueGenericClass.isInstance(value) || !hasCorrectType(key,
                    keyGeneric) || !hasCorrectType(value, valueGeneric)) {
                return false;
            }
        }

        return true;
    }
}
package com.pixelatedslice.easyconfig.api.config.node.factory.builder;

import com.pixelatedslice.easyconfig.api.config.node.Node;
import com.pixelatedslice.easyconfig.api.config.node.collection.CollectionNode;
import com.pixelatedslice.easyconfig.api.config.node.container.ContainerNode;
import com.pixelatedslice.easyconfig.api.validator.null_policy.NullPolicies;
import com.pixelatedslice.easyconfig.api.validator.null_policy.NullPolicy;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@SuppressWarnings({"unused", "UtilityClassWithoutPrivateConstructor", "NonFinalUtilityClass"})
@NullMarked
public class Formats {
    public static final Collection<Format> FORMATS = ServiceLoader
            .load(Format.class)
            .stream()
            .map(ServiceLoader.Provider::get)
            .collect(Collectors.toUnmodifiableSet());
}
package com.pixelatedslice.easyconfig.api.config.node.factory;

import com.google.common.reflect.TypeToken;
import com.pixelatedslice.easyconfig.api.config.node.env.EnvNode;
import com.pixelatedslice.easyconfig.api.config.node.factory.builder.NodeBuilderEnvStep;
import com.pixelatedslice.easyconfig.api.config.node.factory.builder.NodeBuilderGroupStep;
import com.pixelatedslice.easyconfig.api.config.node.factory.builder.NodeBuilderKeySteps;
import com.pixelatedslice.easyconfig.api.config.node.factory.spi.NodeFactoryService;
import com.pixelatedslice.easyconfig.api.config.node.value.ValueNode;
import com.pixelatedslice.easyconfig.api.utils.typetoken.TypeTokenUtils;
import com.pixelatedslice.easyconfig.api.validator.Validator;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

@SuppressWarnings("unused")
@NullMarked
public final class ValidationOptions {

    private ValidationOptions() {
    }

    public static <T> ValidateOption<T> returnEmpty() {
        return (T _, ValidatorContext _) -> Optional.empty();
    }

    public static <T> ValidateOption<T> throwExceptions() {
        return (T _, ValidatorContext context) -> {
            throw new ValidationException(context);
        };
    }

    public static <T> ValidateOption<T> ignoreValidation() {
        return (@Nullable T value, ValidatorContext _) -> Optional.ofNullable(value);
    }
}
package com.pixelatedslice.easyconfig.api.config.node.factory;

import com.google.common.reflect.TypeToken;
import org.jspecify.annotations.NullMarked;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.util.UUID;

@SuppressWarnings("unused")
@NullMarked
public class NodeException extends RuntimeException {
    public NodeException(String message) {
        Objects.requireNonNull(message);

        super(message);
    }

    public NodeException(String message, Object... args) {
        Objects.requireNonNull(message);
        Objects.requireNonNull(args);

        super(String.format(message, args));
    }

    public static NodeException DID_NOT_EXPECT_NODE_TYPE(String key, NodeType expected, NodeType received) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(expected);
        Objects.requireNonNull(received);

        return new NodeException("Type mismatch for node '%s': Expected %s but encountered %s.",
                key,
                expected.toString(),
                received);
    }

    public static NodeException DID_NOT_EXPECT_NODE_TYPE_EXPECTED_VALUE_NODE_BASED(String key, NodeType received) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(received);

        return new NodeException("Type mismatch for node '%s': Expected %s but encountered %s.",
                key,
                String.join(" / ", NodeType.valueNodeBased()),
                received);
    }

    public static NodeException NODE_HAS_WRONG_VALUE_TYPE(String key, TypeToken<?> nodeType, String expectedType) {
        Objects.requireNonNull(key);
        Objects.requireNonNull(nodeType);
        Objects.requireNonNull(expectedType);

        return new NodeException("Value type mismatch for node '%s': Expected %s but found %s.",
                key,
                expectedType,
                nodeType.toString());
    }
}
package com.pixelatedslice.easyconfig.api.config.node.factory.spi;

import com.google.common.reflect.TypeToken;
import com.pixelatedslice.easyconfig.api.config.node.env.EnvNode;
import com.pixelatedslice.easyconfig.api.config.node.factory.builder.NodeBuilderEnvStep;
import com.pixelatedslice.easyconfig.api.config.node.factory.builder.NodeBuilderGroupStep;
import com.pixelatedslice.easyconfig.api.config.node.factory.builder.NodeBuilderKeySteps;
import com.pixelatedslice.easyconfig.api.config.node.value.ValueNode;
import com.pixelatedslice.easyconfig.api.validator.Validator;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ServiceLoader;
import java.util.function.Function;

@SuppressWarnings("unused")
@NullMarked
public class SerializerException extends RuntimeException {
    public SerializerException(String message) {
        Objects.requireNonNull(message);

        super(message);
    }

    public SerializerException(String message, Object... args) {
        Objects.requireNonNull(message);
        Objects.requireNonNull(args);

        super(String.format(message, args));
    }

    public static SerializerException DID_NOT_EXPECT_SERIALIZER_TYPE(SerializerType expected, SerializerType received) {
        Objects.requireNonNull(expected);
        Objects.requireNonNull(received);

        return new SerializerException("Type mismatch for serializer: Expected %s but encountered %s.",
                expected.toString(),
                received);
    }

    public static SerializerException SERIALIZER_HAS_WRONG_SUPPORTED_TYPE(TypeToken<?> nodeType,
            TypeToken<?> expectedType) {
        Objects.requireNonNull(nodeType);
        Objects.requireNonNull(expectedType);

        return new SerializerException("Supported type mismatch for serializer: Expected %s but found %s.",
                expectedType,
                nodeType.toString());
    }
}
package com.pixelatedslice.easyconfig.api.config.node;

import com.google.errorprone.annotations.CheckReturnValue;
import com.pixelatedslice.easyconfig.api.config.ConfigStructure;
import com.pixelatedslice.easyconfig.api.config.node.factory.builder.NodeBuilder;
import org.jspecify.annotations.NullMarked;

import java.util.stream.Stream;

@SuppressWarnings("unused")
@NullMarked
public class TypeException extends RuntimeException {
    public TypeException(String message) {
        Objects.requireNonNull(message);

        super(message);
    }

    public TypeException(String message, Object... args) {
        Objects.requireNonNull(message);
        Objects.requireNonNull(args);

        super(String.format(message, args));
    }

    public static TypeException CLASS_USED_IN_PLACE_OF_TYPETOKEN(TypeToken<?> complex, Class<?> simple) {
        Objects.requireNonNull(complex);
        Objects.requireNonNull(simple);

        return new TypeException(
                "Type safety violation: Cannot use simple Class (%s) to evaluate complex TypeToken (%s).",
                simple.getSimpleName(),
                complex.toString());
    }

    public static TypeException CLASS_USED_IN_PLACE_OF_TYPETOKEN(Class<?> simple) {
        Objects.requireNonNull(simple);

        return new TypeException(
                "Type safety violation: Simple Class (%s) cannot be used where a complex TypeToken is required.",
                simple.getSimpleName());
    }

    public static TypeException TYPES_DO_NOT_MATCH(TypeToken<?> expected, TypeToken<?> received) {
        Objects.requireNonNull(expected);
        Objects.requireNonNull(received);

        return new TypeException("Type safety violation: Incompatible types. Expected %s but received %s.",
                expected.toString(),
                received.toString());
    }
}
package com.pixelatedslice.easyconfig.api.config;

import com.pixelatedslice.easyconfig.api.config.node.Node;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ValidationException extends IllegalArgumentException {

    private final ValidatorContext context;

    public ValidationException(ValidatorContext context) {
        super("Validation failed\n" + String.join("\n\t- ", context.errors()));
        this.context = context;
    }

    public ValidatorContext context() {
        return this.context;
    }
}
package com.pixelatedslice.easyconfig.api.config;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class DuplicateException extends IllegalArgumentException {

    public DuplicateException(Object original, Object replacement) {
        super(replacement + " cannot replace " + original);
    }
}
package com.pixelatedslice.easyconfig.api.editable;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class SerializeException extends Exception {


}
package com.pixelatedslice.easyconfig.api.editable;

import org.jspecify.annotations.NullMarked;

// All nulls in DefinedEnv are the culprits for DataFlowIssue warnings, but they are handled correctly in the code.
@SuppressWarnings({"unused", "DataFlowIssue"})
@NullMarked
public final class EnvKeys {

    public static final NodeBuilderEnvStep.PreDefined<String> DEBUGGER_ID = new DefinedEnv<>("DEBUGGER_ID",
            (String t) -> t);
    public static final NodeBuilderEnvStep.PreDefined<Boolean> DEBUGGING_ENABLED = new DefinedEnv<>("DEBUGGER_ENABLED",
            Boolean::parseBoolean);
    public static final NodeBuilderEnvStep.PreDefined<@Nullable Integer> DISPLAY = new DefinedEnv<>("DISPLAY",
            (String input) -> {
                try {
                    return Integer.parseInt(input.substring(1));
                } catch (NumberFormatException e) {
                    return null;
                }
            });
    public static final NodeBuilderEnvStep.PreDefined<@Nullable Path> EDITOR = DefinedEnv.path("EDITOR");
    public static final NodeBuilderEnvStep.PreDefined<@Nullable Path> HOME = DefinedEnv.path("HOME");
    public static final NodeBuilderEnvStep.PreDefined<@Nullable LangResult> LANG = new DefinedEnv<>("LANG",
            (String input) -> {
                String[] split = input.split("\\.", 2);
                return (split.length != 2) ? null : new LangResult(Locale.of(split[0]), split[1]);
            });
    public static final NodeBuilderEnvStep.PreDefined<@Nullable Collection<Path>> PATHS = new DefinedEnv<>("PATH",
            (String input) -> {
                var split = input.split(";");
                try {
                    return Stream.of(split).map((String stringPath) -> {
                        try {
                            return Path.of(stringPath).toRealPath();
                        } catch (IllegalArgumentException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    }).toList();
                } catch (RuntimeException e) {
                    return null;
                }
            });
    public static final NodeBuilderEnvStep.PreDefined<@Nullable Path> SHELL = DefinedEnv.path("SHELL");
    public static final NodeBuilderEnvStep.PreDefined<String> USER = new DefinedEnv<>("USER", (String t) -> t);

    private EnvKeys() {

    }

    public record LangResult(Locale language, String encoding) {

    }

    @NullMarked
    record DefinedEnv<T extends @Nullable Object>(String key, Function<String, @Nullable T> adapter)
            implements NodeBuilderEnvStep.PreDefined<T> {

        @SuppressWarnings("DataFlowIssue")
        static DefinedEnv<@Nullable Path> path(String key) {
            return new DefinedEnv<>(key, (String input) -> {
                try {
                    return Path.of(input).toRealPath();
                } catch (IllegalArgumentException | IOException ex) {
                    return null; // The culprit
                }
            });
        }

    }
}
open module com.pixelatedslice.easyconfig.api {
    uses com.pixelatedslice.easyconfig.api.serialization.SerializerRegistry;
    uses com.pixelatedslice.easyconfig.api.format.Format;
    uses com.pixelatedslice.easyconfig.api.config.node.factory.spi.NodeFactoryService;
    requires com.google.common;
    requires org.jspecify;
    requires com.google.errorprone.annotations;
    requires org.jetbrains.annotations;

    exports com.pixelatedslice.easyconfig.api.config.node;
    exports com.pixelatedslice.easyconfig.api.config.node.collection;
    exports com.pixelatedslice.easyconfig.api.config.node.container;
    exports com.pixelatedslice.easyconfig.api.config.node.value;
    exports com.pixelatedslice.easyconfig.api.config.node.env;
    exports com.pixelatedslice.easyconfig.api.editable;
    exports com.pixelatedslice.easyconfig.api.exception;
    exports com.pixelatedslice.easyconfig.api.format;
    exports com.pixelatedslice.easyconfig.api.serialization;
    exports com.pixelatedslice.easyconfig.api.utils.typetoken;
    exports com.pixelatedslice.easyconfig.api.utils.primitive;
    exports com.pixelatedslice.easyconfig.api.validator;
    exports com.pixelatedslice.easyconfig.api.validator.option;
    exports com.pixelatedslice.easyconfig.api.validator.null_policy;
    exports com.pixelatedslice.easyconfig.api.config;
    exports com.pixelatedslice.easyconfig.api.config.node.factory;
    exports com.pixelatedslice.easyconfig.api.config.node.factory.builder;
    exports com.pixelatedslice.easyconfig.api.config.node.factory.spi;
}
