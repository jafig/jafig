package com.pixelatedslice.easyconfig.impl.validator;

import com.pixelatedslice.easyconfig.api.validator.ValidatorContext;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@NullMarked
public interface InternalNodeBuilder<Self> {

    @CheckReturnValue
    Self parent(@Nullable AbstractNode node);

    @Nullable
    AbstractNode parent();

    @CheckReturnValue
    Self config(@Nullable Config config);

    @Nullable
    Config config();

    @Nullable
    String key();

    Collection<InternalNodeBuilder<?>> children();

    void appendChild(InternalNodeBuilder<?> builder);

    AbstractNode build();

    default void buildChildren(AbstractNode built) {
        @SuppressWarnings("ResultOfMethodCallIgnored")
        final var immediateChildren = this
                .children()
                .stream()
                .peek((InternalNodeBuilder<?> builder) -> builder.parent(built))
                .collect(Collectors.toMap((InternalNodeBuilder<?> t) -> t, InternalNodeBuilder::build));
        for (var child : immediateChildren.values()) {
            built.internalAppendChild(child);
        }
        for (var entry : immediateChildren.entrySet()) {
            entry.getKey().buildChildren(entry.getValue());
        }

    }

}
package com.pixelatedslice.easyconfig.impl.config.node.collection.builder;

import com.pixelatedslice.easyconfig.api.config.Config;
import com.pixelatedslice.easyconfig.api.config.node.Node;
import com.pixelatedslice.easyconfig.api.config.node.factory.builder.NodeBuilderGroupStep;
import com.pixelatedslice.easyconfig.api.validator.null_policy.NullPolicy;
import com.pixelatedslice.easyconfig.impl.config.node.AbstractNode;
import com.pixelatedslice.easyconfig.impl.config.node.InternalNodeBuilder;
import com.pixelatedslice.easyconfig.impl.config.node.collection.CollectionNodeImpl;
import com.pixelatedslice.easyconfig.impl.config.node.container.builder.ContainerNodeBuilder;
import com.pixelatedslice.easyconfig.impl.config.node.factory.AbstractGroupNodeBuilder;
import com.pixelatedslice.easyconfig.impl.config.node.factory.NodeBuilderHandlers;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@NullMarked
public interface NodeBuilderHandlers {
    interface Value<T>
            extends NodeBuilderKeySteps.Value<T>, NodeBuilderValueStep<T>, NodeBuilder.BuildStep<ValueNode<T>> {
    }

    interface Env<T> extends NodeBuilderKeySteps.Env<T>, NodeBuilderEnvStep.VariableStep<T>,
            NodeBuilderEnvStep.AdapterValidatorStep<T>, NodeBuilder.BuildStep<EnvNode<T>> {
    }

    interface Container extends NodeBuilderKeySteps.Container, NodeBuilderGroupStep.Container {
    }

    interface Collection extends NodeBuilderKeySteps.Collection, NodeBuilderGroupStep.Collection {
    }
}
package com.pixelatedslice.easyconfig.impl.config.node.collection;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.pixelatedslice.easyconfig.api.config.node.Node;
import com.pixelatedslice.easyconfig.api.config.node.ReturnedNode;
import com.pixelatedslice.easyconfig.api.config.node.collection.CollectionNode;
import com.pixelatedslice.easyconfig.impl.config.node.AbstractNode;
import com.pixelatedslice.easyconfig.impl.config.node.InternalNodeBuilder;
import com.pixelatedslice.easyconfig.impl.config.node.ReturnKnownNodeImpl;
import com.pixelatedslice.easyconfig.impl.config.node.collection.builder.CollectionNodeBuilder;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

@NullMarked
public class ValidatorContextImpl implements ValidatorContext {

    private final Map<String, Object[]> errors = new ConcurrentHashMap<>();

    @Override
    public boolean hasError() {
        return !this.errors.isEmpty();
    }

    public Collection<String> errors() {
        return this.errors.entrySet().stream().map(entry -> entry.getKey().formatted(entry.getValue())).toList();
    }

    @Override
    public void error(String message, Object... variables) {
        this.errors.put(message, variables);
    }

}
package com.pixelatedslice.easyconfig.impl.config.node.container.builder;

import com.pixelatedslice.easyconfig.api.config.Config;
import com.pixelatedslice.easyconfig.api.config.node.Node;
import com.pixelatedslice.easyconfig.api.config.node.factory.builder.NodeBuilderGroupStep;
import com.pixelatedslice.easyconfig.api.validator.null_policy.NullPolicy;
import com.pixelatedslice.easyconfig.impl.config.node.AbstractNode;
import com.pixelatedslice.easyconfig.impl.config.node.InternalNodeBuilder;
import com.pixelatedslice.easyconfig.impl.config.node.container.ContainerNodeImpl;
import com.pixelatedslice.easyconfig.impl.config.node.factory.AbstractGroupNodeBuilder;
import com.pixelatedslice.easyconfig.impl.config.node.factory.NodeBuilderHandlers;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

@NullMarked
public class CollectionNodeBuilder extends AbstractGroupNodeBuilder
        implements NodeBuilderHandlers.Collection, InternalNodeBuilder<CollectionNodeBuilder> {

    private String key;
    private @Nullable Config config;
    private @Nullable AbstractNode parent;

    public CollectionNodeBuilder(String key) {
        this.key = key;
    }

    @Override
    public CollectionNodeBuilder parent(@Nullable AbstractNode node) {
        this.parent = node;
        return this;
    }

    @Override
    public @Nullable AbstractNode parent() {
        return this.parent;
    }

    @Override
    public CollectionNodeBuilder config(@Nullable Config config) {
        this.config = config;
        return this;
    }

    @Override
    public @Nullable Config config() {
        return this.config;
    }

    @Override
    public @Nullable String key() {
        return this.key;
    }

    @Override
    public List<InternalNodeBuilder<?>> children() {
        return Collections.unmodifiableList(this.children);
    }

    @Override
    public void appendChild(InternalNodeBuilder<?> builder) {
        if (builder instanceof ContainerNodeBuilder containerBuilder) {
            this.children.add(containerBuilder);
            return;
        }
        if (builder instanceof CollectionNodeBuilder collectionBuilder) {
            this.children.add(collectionBuilder);
            return;
        }
        throw new IllegalArgumentException("Cannot append child of " + builder.getClass().getName());
    }

    @Override
    public NodeBuilderGroupStep.Collection children(NullPolicy nullPolicy, @Nullable BuildStep<?> @Nullable ... nodes) {
        if (nodes == null) {
            return this;
        }
        this.internalChildren(nullPolicy, Arrays.stream(nodes));
        return this;
    }

    @Override
    public NodeBuilderGroupStep.Collection children(NullPolicy nullPolicy,
            java.util.@Nullable Collection<? extends @Nullable BuildStep<?>> nodes) {
        if (nodes == null) {
            return this;
        }
        this.internalChildren(nullPolicy, nodes.stream());
        return this;
    }

    @Override
    public NodeBuilderGroupStep.Collection builtChildren(NullPolicy nullPolicy, @Nullable Node @Nullable ... nodes) {
        if (nodes == null) {
            return this;
        }
        this.internalBuiltChildren(nullPolicy, Arrays.stream(nodes));
        return this;
    }

    @Override
    public NodeBuilderGroupStep.Collection builtChildren(NullPolicy nullPolicy,
            java.util.@Nullable Collection<? extends Node> nodes) {
        if (nodes == null) {
            return this;
        }
        this.internalBuiltChildren(nullPolicy, nodes.stream());
        return this;
    }

    @Override
    public CollectionNodeImpl build() {
        final var result = new CollectionNodeImpl(this);
        this.buildChildren(result);
        return result;
    }

    @Override
    public NodeBuilderGroupStep.Collection key(String key) {
        this.key = key;
        return this;
    }
}
package com.pixelatedslice.easyconfig.impl.config.node.container;

import com.pixelatedslice.easyconfig.api.config.node.Node;
import com.pixelatedslice.easyconfig.api.config.node.container.EditableContainerNode;
import com.pixelatedslice.easyconfig.impl.config.node.AbstractNode;
import com.pixelatedslice.easyconfig.impl.utils.DistinctByGatherer;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.LinkedTransferQueue;
import java.util.function.Function;
import java.util.stream.Stream;

@NullMarked
public class CollectionNodeImpl extends AbstractNode implements CollectionNode {
    private final List<AbstractNode> children = new CopyOnWriteArrayList<>();

    public CollectionNodeImpl(InternalNodeBuilder<?> builder) {
        super(builder);
    }

    @Override
    public ImmutableCollection<ReturnedNode> nodes() {
        return ImmutableList.copyOf(this.children.stream().map(ReturnKnownNodeImpl::new).toList());
    }

    @Override
    public Stream<ReturnedNode> stream() {
        return this.children.stream().map(ReturnKnownNodeImpl::new);
    }

    @Override
    public ReturnedNode atIndex(int index) {
        final Node child = ((index < 0) || (index >= this.children.size())) ? null : this.children.get(index);

        return new ReturnKnownNodeImpl(child);
    }

    @Override
    protected void internalAppendChild(AbstractNode node) {
        this.children.add(node);
    }

    @Override
    public CollectionNodeBuilder toBuilder() {
        final var builder = new CollectionNodeBuilder(this.key()).parent(this.parent).config(this.attached);
        this.children
                .stream()
                .map(AbstractNode::toBuilder)
                .map(keyStep -> (InternalNodeBuilder<?>) keyStep)
                .forEach(builder::appendChild);
        return builder;
    }

    @Override
    public String toString() {
        final var joiner = new java.util.StringJoiner(", ", "[", "]");
        for (final var child : this.children) {
            joiner.add(child.key());
        }

        return "CollectionNodeImpl{"
               + "key='"
               + this.key()
               + '\''
               + ", childCount="
               + this.children.size()
               + ", children="
               + joiner
               + ", fullPath="
               + String.join(",", this.fullPath())
               + '}';
    }
}
package com.pixelatedslice.easyconfig.impl.config.node.container;

import com.google.common.collect.ImmutableList;
import com.pixelatedslice.easyconfig.api.config.node.Node;
import com.pixelatedslice.easyconfig.api.config.node.ReturnedNode;
import com.pixelatedslice.easyconfig.api.config.node.container.ContainerNode;
import com.pixelatedslice.easyconfig.api.config.node.container.EditableContainerNode;
import com.pixelatedslice.easyconfig.impl.config.node.AbstractNode;
import com.pixelatedslice.easyconfig.impl.config.node.InternalNodeBuilder;
import com.pixelatedslice.easyconfig.impl.config.node.container.builder.ContainerNodeBuilder;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.concurrent.LinkedBlockingQueue;

@NullMarked
public class ContainerNodeBuilder extends AbstractGroupNodeBuilder
        implements NodeBuilderHandlers.Container, InternalNodeBuilder<ContainerNodeBuilder> {

    @Nullable
    AbstractNode parent;
    @Nullable
    Config config;
    private String key;

    public ContainerNodeBuilder(String key) {
        this.key = Objects.requireNonNull(key);
    }

    @Override
    public ContainerNodeBuilder parent(@Nullable AbstractNode node) {
        this.parent = node;
        return this;
    }

    @Override
    public @Nullable AbstractNode parent() {
        return this.parent;
    }

    @Override
    public ContainerNodeBuilder config(@Nullable Config config) {
        this.config = config;
        return this;
    }

    @Override
    public @Nullable Config config() {
        return this.config;
    }

    public @Nullable String key() {
        return this.key;
    }

    @Override
    public java.util.Collection<InternalNodeBuilder<?>> children() {
        return Collections.unmodifiableCollection(this.children);
    }

    @Override
    public void appendChild(InternalNodeBuilder<?> builder) {
        this.children.add(builder);
    }

    @Override
    public ContainerNodeImpl build() {
        final var built = new ContainerNodeImpl(this);
        this.buildChildren(built);
        return built;
    }

    @Override
    public NodeBuilderGroupStep.Container children(NullPolicy nullPolicy, @Nullable BuildStep<?> @Nullable ... nodes) {
        if (nodes == null) {
            return this;
        }
        this.internalChildren(nullPolicy, Arrays.stream(nodes));
        return this;
    }

    @Override
    public NodeBuilderGroupStep.Container children(NullPolicy nullPolicy,
            java.util.@Nullable Collection<? extends @Nullable BuildStep<?>> nodes) {
        if (nodes == null) {
            return this;
        }
        this.internalChildren(nullPolicy, nodes.stream());
        return this;
    }

    @Override
    public NodeBuilderGroupStep.Container builtChildren(NullPolicy nullPolicy, @Nullable Node @Nullable ... nodes) {
        if (nodes == null) {
            return this;
        }
        this.internalBuiltChildren(nullPolicy, Arrays.stream(nodes));
        return this;
    }

    @Override
    public NodeBuilderGroupStep.Container builtChildren(NullPolicy nullPolicy,
            java.util.@Nullable Collection<? extends Node> nodes) {
        if (nodes == null) {
            return this;
        }
        this.internalBuiltChildren(nullPolicy, nodes.stream());
        return this;
    }

    @Override
    public NodeBuilderGroupStep.Container key(String key) {
        this.key = key;
        return this;
    }
}
package com.pixelatedslice.easyconfig.impl.config.node.env;

import com.google.common.reflect.TypeToken;
import com.pixelatedslice.easyconfig.api.config.node.env.EnvNode;
import com.pixelatedslice.easyconfig.api.validator.Validator;
import com.pixelatedslice.easyconfig.api.validator.option.ValidateOption;
import com.pixelatedslice.easyconfig.impl.config.node.AbstractNode;
import com.pixelatedslice.easyconfig.impl.config.node.env.builder.EnvNodeBuilder;
import com.pixelatedslice.easyconfig.impl.validator.ValidatorContextImpl;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@NullMarked
public class ContainerNodeEditableImpl implements EditableContainerNode {

    private final ContainerNodeImpl target;
    private final LinkedTransferQueue<Node> removingNode = new LinkedTransferQueue<>();
    private final LinkedTransferQueue<Node> addingNodes = new LinkedTransferQueue<>();

    ContainerNodeEditableImpl(ContainerNodeImpl target) {
        this.target = Objects.requireNonNull(target);
    }

    @Override
    public EditableContainerNode addNodes(Collection<? extends Node> nodes) {
        this.addingNodes.addAll(nodes);
        return this;
    }

    @Override
    public EditableContainerNode setNodes(Collection<? extends Node> nodes) {
        this.clearNodes();
        return this.addNodes(nodes.toArray(Node[]::new));
    }

    @Override
    public EditableContainerNode removeNodes(Node... nodes) {
        return this.removeNodes(Stream.of(nodes).map(Node::key), t -> t);
    }

    @Override
    public EditableContainerNode removeNodes(Collection<? extends Node> nodes) {
        return this.removeNodes(nodes.stream().map(Node::key), t -> t);
    }

    @Override
    public EditableContainerNode removeNodes(String... keys) {
        return this.removeNodes(Stream.of(keys), t -> t);
    }

    @Override
    public EditableContainerNode clearNodes() {
        this.removingNode.addAll(this.target.children());
        return this;
    }

    private EditableContainerNode removeNodes(Stream<String> keys, Function<Stream<Node>, Stream<Node>> function) {
        final var targetChildren = this.target.children().stream().toList();
        final var removingFilter = keys
                .map(key -> targetChildren.stream().filter(n -> n.key().equals(key)).findFirst())
                .filter(Optional::isPresent)
                .map(Optional::orElseThrow);
        function.apply(removingFilter);
        this.removingNode.addAll(removingFilter.toList());
        return this;
    }

    @Override
    public void close() {
        final var distinctToRemove = this.removingNode.stream().gather(new DistinctByGatherer<>(Node::key)).toList();
        this.target.removeNodes(distinctToRemove);

        for (var node : this.addingNodes) {
            if (!(node instanceof AbstractNode)) {
                throw new IllegalArgumentException("Cannot add " + node.getClass().getName() + " to EasyConfig node");
            }
        }

        //TODO to builder -> swap parent to this
        final var distinctToAdd = this.addingNodes
                .stream()
                .map(n -> (AbstractNode) n)
                .gather(new DistinctByGatherer<>(Node::key))
                .toList();
        this.target.internalAppendChildren(distinctToAdd);
    }
}
package com.pixelatedslice.easyconfig.impl.config.node.env.builder;

import com.google.common.reflect.TypeToken;
import com.pixelatedslice.easyconfig.api.config.Config;
import com.pixelatedslice.easyconfig.api.config.node.factory.builder.NodeBuilderEnvStep;
import com.pixelatedslice.easyconfig.api.validator.Validator;
import com.pixelatedslice.easyconfig.impl.config.node.AbstractNode;
import com.pixelatedslice.easyconfig.impl.config.node.InternalNodeBuilder;
import com.pixelatedslice.easyconfig.impl.config.node.env.EnvNodeImpl;
import com.pixelatedslice.easyconfig.impl.config.node.factory.NodeBuilderHandlers;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;

@NullMarked
public class ContainerNodeImpl extends AbstractNode implements ContainerNode {
    private final Collection<Node> immediateChildren = new LinkedBlockingQueue<>();
    private String toString = this.generateToString();

    public ContainerNodeImpl(InternalNodeBuilder<?> builder) {
        super(builder);
    }

    @Override
    public boolean isRootNode() {
        return this.parent == null;
    }

    @Override
    protected synchronized void internalAppendChild(AbstractNode node) {
        this.immediateChildren.add(node);
        this.toString = this.generateToString();
    }

    protected synchronized void internalAppendChildren(Collection<? extends AbstractNode> node) {
        this.immediateChildren.addAll(node);
        this.toString = this.generateToString();
    }

    @Override
    public ImmutableList<Node> children() {
        return ImmutableList.<Node>builder().addAll(this.immediateChildren).build();
    }

    @Override
    public ReturnedNode node(String... path) {
        return travel(this, path);
    }


    synchronized void removeNodes(Collection<Node> nodes) {
        this.immediateChildren.removeAll(nodes);
    }

    @Override
    public EditableContainerNode editable() {
        return new ContainerNodeEditableImpl(this);
    }

    @Override
    public ContainerNodeBuilder toBuilder() {
        return new ContainerNodeBuilder(this.key()).parent(this.parent).config(this.attached);
    }

    private String generateToString() {
        final var joiner = new java.util.StringJoiner(", ", "[", "]");
        for (final var child : this.immediateChildren) {
            joiner.add(child.key());
        }

        return "ContainerNodeImpl{"
               + "key='"
               + this.key()
               + '\''
               + ", childCount="
               + this.immediateChildren.size()
               + ", children="
               + joiner
               + ", fullPath="
               + String.join(",", this.fullPath())
               + '}';
    }

    @Override
    public String toString() {
        return this.toString;
    }
}
package com.pixelatedslice.easyconfig.impl.config.node.value;

import com.pixelatedslice.easyconfig.api.config.node.value.EditableValueNode;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.atomic.AtomicReference;

@NullMarked
public class EnvNodeImpl<T> extends AbstractNode implements EnvNode<T> {
    private final String toString;
    private final String envKey;
    private final Function<String, @Nullable T> adapter;
    private final TypeToken<T> type;
    private final Validator<T> validator;

    public EnvNodeImpl(EnvNodeBuilder<T> builder) {
        super(builder);
        this.envKey = Objects.requireNonNull(builder.envKey());
        this.adapter = Objects.requireNonNull(builder.adapter());
        this.type = Objects.requireNonNull(builder.type());
        this.validator = Objects.requireNonNullElseGet(builder.validator(), Validator::empty);

        this.toString = "EnvNodeImpl{"
                        + "key='"
                        + this.key()
                        + '\''
                        + ", envKey='"
                        + this.envKey
                        + '\''
                        + ", type="
                        + this.type
                        + ", fullPath="
                        + String.join(",", this.fullPath())
                        + '}';
    }

    @Override
    public String envKey() {
        return this.envKey;
    }

    @Override
    public Optional<T> value(ValidateOption<T> option) {
        final var envValue = System.getenv(this.envKey);
        return (envValue == null)
                ? Optional.empty()
                : Optional.ofNullable(this.adapter.apply(envValue)).flatMap(value -> {
                    final var context = new ValidatorContextImpl();
                    this.validator.validate(value, context);
                    return context.hasError() ? option.onValidationError(value, context) : Optional.of(value);
                });
    }

    @Override
    public Function<String, @Nullable T> adapter() {
        return this.adapter;
    }

    @Override
    public Validator<T> validator() {
        return this.validator;
    }

    @Override
    public TypeToken<T> typeToken() {
        return this.type;
    }

    @Override
    protected void internalAppendChild(AbstractNode node) {
        throw new IllegalArgumentException("child nodes are not supported on EnvNode");
    }

    @Override
    public EnvNodeBuilder<T> toBuilder() {
        return (EnvNodeBuilder<T>) new EnvNodeBuilder<>(this.type)
                .config(this.attached)
                .parent(this.parent)
                .adapter(this.adapter)
                .key(this.key())
                .variable(this.envKey);
    }

    @Override
    public String toString() {
        return this.toString;
    }
}
package com.pixelatedslice.easyconfig.impl.config.node.value;

import com.google.common.reflect.TypeToken;
import com.pixelatedslice.easyconfig.api.config.node.value.EditableValueNode;
import com.pixelatedslice.easyconfig.api.config.node.value.ValueNode;
import com.pixelatedslice.easyconfig.api.serialization.Serializer;
import com.pixelatedslice.easyconfig.api.validator.Validator;
import com.pixelatedslice.easyconfig.api.validator.option.ValidateOption;
import com.pixelatedslice.easyconfig.api.validator.option.ValidationOptions;
import com.pixelatedslice.easyconfig.impl.config.node.AbstractNode;
import com.pixelatedslice.easyconfig.impl.config.node.value.builder.ValueNodeBuilder;
import com.pixelatedslice.easyconfig.impl.validator.ValidatorContextImpl;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("PublicMethodNotExposedInInterface")
@NullMarked
public class EnvNodeBuilder<T> implements NodeBuilderHandlers.Env<T>, InternalNodeBuilder<EnvNodeBuilder<T>> {

    private final TypeToken<T> typeToken;
    private @Nullable String key;
    private @Nullable String variable;
    private @Nullable AbstractNode parent;
    private @Nullable Config config;
    private @Nullable Function<String, @Nullable T> adapter;
    private @Nullable Validator<T> validator = Validator.empty();

    public EnvNodeBuilder(TypeToken<T> typeToken) {
        this.typeToken = Objects.requireNonNull(typeToken);
    }

    public EnvNodeBuilder(TypeToken<T> typeToken, String key, String variable) {
        this.typeToken = typeToken;
        this.key = key;
        this.variable = variable;
    }

    public Validator<T> validator() {
        return Objects.requireNonNull(this.validator);
    }

    public @Nullable Function<String, @Nullable T> adapter() {
        return Objects.requireNonNull(this.adapter);
    }

    public TypeToken<T> type() {
        return this.typeToken;
    }

    public String envKey() {
        return Objects.requireNonNull(this.variable);
    }

    @Override
    public EnvNodeBuilder<T> parent(@Nullable AbstractNode node) {
        this.parent = node;
        return this;
    }

    @Override
    public @Nullable AbstractNode parent() {
        return this.parent;
    }

    @Override
    public EnvNodeBuilder<T> config(@Nullable Config config) {
        this.config = config;
        return this;
    }

    @Override
    public @Nullable Config config() {
        return this.config;
    }

    @Override
    public @Nullable String key() {
        return this.key;
    }

    @Override
    public java.util.Collection<InternalNodeBuilder<?>> children() {
        return Collections.emptyList();
    }

    @Override
    public void appendChild(InternalNodeBuilder<?> builder) {
        throw new IllegalArgumentException("Cannot append to Env");
    }

    @Override
    public EnvNodeImpl<T> build() {
        return new EnvNodeImpl<>(this);
    }

    @Override
    public EnvNodeBuilder<T> adapter(@Nullable Function<String, @Nullable T> adapter) {
        this.adapter = adapter;
        return this;
    }

    @Override
    public EnvNodeBuilder<T> validator(@Nullable Validator<T> validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public NodeBuilderEnvStep.AdapterValidatorStep<T> variable(String variable) {
        this.variable = variable;
        return this;
    }

    @Override
    public NodeBuilderEnvStep.VariableStep<T> key(String key) {
        this.key = key;
        return this;
    }
}
package com.pixelatedslice.easyconfig.impl.config.node.value.builder;

import com.google.common.reflect.TypeToken;
import com.pixelatedslice.easyconfig.api.config.Config;
import com.pixelatedslice.easyconfig.api.config.node.factory.builder.NodeBuilderValueStep;
import com.pixelatedslice.easyconfig.api.serialization.Serializer;
import com.pixelatedslice.easyconfig.api.validator.Validator;
import com.pixelatedslice.easyconfig.impl.config.node.AbstractNode;
import com.pixelatedslice.easyconfig.impl.config.node.InternalNodeBuilder;
import com.pixelatedslice.easyconfig.impl.config.node.factory.NodeBuilderHandlers;
import com.pixelatedslice.easyconfig.impl.config.node.value.ValueNodeImpl;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

@NullMarked
class EditableValueNodeImpl<T> implements EditableValueNode<T> {

    private final ValueNodeImpl<T> modifying;
    private final AtomicReference<@Nullable T> valueChangingTo = new AtomicReference<>();

    EditableValueNodeImpl(ValueNodeImpl<T> modifying) {
        this.modifying = modifying;
    }

    @Override
    public EditableValueNode<T> setValue(@Nullable T value) {
        this.valueChangingTo.set(value);
        return this;
    }

    @Override
    public void close() {
        this.modifying.internalSetValue(this.valueChangingTo.get());
    }
}
package com.pixelatedslice.easyconfig.impl.config.node;

import com.pixelatedslice.easyconfig.api.config.Config;
import com.pixelatedslice.easyconfig.api.config.ConfigStructure;
import com.pixelatedslice.easyconfig.api.config.node.Node;
import com.pixelatedslice.easyconfig.api.config.node.ReturnedNode;
import com.pixelatedslice.easyconfig.api.config.node.collection.CollectionNode;
import com.pixelatedslice.easyconfig.api.config.node.container.ContainerNode;
import com.pixelatedslice.easyconfig.api.config.node.factory.builder.NodeBuilder;
import com.pixelatedslice.easyconfig.impl.config.ConfigStructureImpl;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.stream.Stream;

@NullMarked
public class ValueNodeImpl<T> extends AbstractNode implements ValueNode<T> {
    private final TypeToken<T> token;
    private final Validator<T> validator;
    private final @Nullable Serializer<T> serializer;
    private final @Nullable T defaultValue;
    private @Nullable T value;
    private String toString = this.generateToString();

    public ValueNodeImpl(ValueNodeBuilder<T> builder) {
        super(builder);
        this.defaultValue = builder.defaultValue();
        this.token = Objects.requireNonNull(builder.type());
        this.validator = Objects.requireNonNullElseGet(builder.validator(), Validator::empty);
        this.serializer = builder.serializer();
        this.value = builder.value();
    }

    synchronized void internalSetValue(@Nullable T value) {
        this.value = value;
        this.toString = this.generateToString();
    }

    @Override
    public Optional<T> value(ValidateOption<T> option) {
        return Optional.ofNullable(this.value).flatMap((T value) -> this.validate(option, value));
    }

    @Override
    public Optional<T> defaultValue(ValidateOption<T> option) {
        return Optional.ofNullable(this.defaultValue).flatMap((T defaultValue) -> this.validate(option, defaultValue));
    }

    @Override
    public Optional<T> valueOrDefault(ValidateOption<T> option) {
        return this.value(ValidationOptions.returnEmpty()).or(() -> this.defaultValue(option));
    }

    private Optional<T> validate(ValidateOption<T> option, @Nullable T validationValue) {
        final var context = new ValidatorContextImpl();
        this.validator.validate(validationValue, context);
        return context.hasError()
                ? option.onValidationError(validationValue, context)
                : Optional.ofNullable(validationValue);
    }

    @Override
    public Optional<Serializer<T>> serializer() {
        return Optional.ofNullable(this.serializer);
    }

    @Override
    public Validator<T> validator() {
        return this.validator;
    }

    @Override
    public TypeToken<T> typeToken() {
        return this.token;
    }

    @Override
    public EditableValueNode<T> editable() {
        return new EditableValueNodeImpl<>(this);
    }

    @Override
    public ValueNodeBuilder<T> toBuilder() {
        return (ValueNodeBuilder<T>) new ValueNodeBuilder<>(this.token)
                .key(this.key())
                .defaultValue(this.defaultValue)
                .value(this.value)
                .serializer(this.serializer)
                .validator(this.validator);
    }

    @Override
    protected void internalAppendChild(AbstractNode node) {
        throw new IllegalStateException("Value node! should not have called");
    }

    private String generateToString() {
        return "ValueNodeImpl{"
               + "key='"
               + this.key()
               + '\''
               + ", type="
               + this.token
               + ", value="
               + this.value
               + ", defaultValue="
               + this.defaultValue
               + ", fullPath="
               + String.join(",", this.fullPath())
               + '}';
    }

    @Override
    public String toString() {
        return this.toString;
    }
}
package com.pixelatedslice.easyconfig.impl.config.node;

import com.google.errorprone.annotations.CheckReturnValue;
import com.pixelatedslice.easyconfig.api.config.Config;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.stream.Collectors;

@SuppressWarnings("PublicMethodNotExposedInInterface")
@NullMarked
public class ValueNodeBuilder<T> implements NodeBuilderHandlers.Value<T>, InternalNodeBuilder<ValueNodeBuilder<T>> {

    @Nullable
    String key;
    @Nullable
    T defaultValue;
    @Nullable
    T value;
    TypeToken<T> typeToken;
    @Nullable
    Validator<T> validator;
    @Nullable
    Serializer<T> serializer;
    @Nullable
    Config config;
    @Nullable
    AbstractNode parent;
    java.util.Collection<InternalNodeBuilder<?>> children = new CopyOnWriteArrayList<>();

    public ValueNodeBuilder(TypeToken<T> token) {
        this.typeToken = Objects.requireNonNull(token);
    }

    public ValueNodeBuilder(TypeToken<T> typeToken, String key, @Nullable T defaultValue, @Nullable T value) {
        this.typeToken = typeToken;
        this.key = key;
        this.defaultValue = defaultValue;
        this.value = value;
    }

    public String key() {
        return Objects.requireNonNull(this.key);
    }

    public @Nullable T defaultValue() {
        return this.defaultValue;
    }

    public @Nullable T value() {
        return this.value;
    }

    public TypeToken<T> type() {
        return this.typeToken;
    }

    @Override
    public NodeBuilderValueStep<T> defaultValue(@Nullable T defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public NodeBuilderValueStep<T> value(@Nullable T value) {
        this.value = value;
        return this;
    }

    @Override
    public NodeBuilderValueStep<T> validator(@Nullable Validator<T> validator) {
        this.validator = validator;
        return this;
    }

    public @Nullable Validator<T> validator() {
        return this.validator;
    }

    public @Nullable Serializer<T> serializer() {
        return this.serializer;
    }

    @Override
    public NodeBuilderValueStep<T> serializer(@Nullable Serializer<T> serializer) {
        this.serializer = serializer;
        return this;
    }

    @Override
    public ValueNodeBuilder<T> parent(@Nullable AbstractNode node) {
        this.parent = node;
        return this;
    }

    @Override
    public @Nullable AbstractNode parent() {
        return this.parent;
    }

    @Override
    public ValueNodeBuilder<T> config(@Nullable Config config) {
        this.config = config;
        return this;
    }

    @Override
    public @Nullable Config config() {
        return this.config;
    }

    @Override
    public java.util.Collection<InternalNodeBuilder<?>> children() {
        return Collections.unmodifiableCollection(this.children);
    }

    @Override
    public void appendChild(InternalNodeBuilder<?> builder) {
        this.children.add(builder);
    }

    @Override
    public ValueNodeImpl<T> build() {
        return new ValueNodeImpl<>(this);
    }

    @Override
    public NodeBuilderValueStep<T> key(String key) {
        this.key = key;
        return this;
    }
}
package com.pixelatedslice.easyconfig.impl.config.node;

import com.google.common.reflect.TypeToken;
import com.pixelatedslice.easyconfig.api.config.node.Node;
import com.pixelatedslice.easyconfig.api.config.node.ReturnedNode;
import com.pixelatedslice.easyconfig.api.config.node.collection.CollectionNode;
import com.pixelatedslice.easyconfig.api.config.node.container.ContainerNode;
import com.pixelatedslice.easyconfig.api.config.node.env.EnvNode;
import com.pixelatedslice.easyconfig.api.config.node.value.ValueNode;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

@NullMarked
public abstract class AbstractNode implements Node {

    protected final @Nullable AbstractNode parent;
    protected final @Nullable Config attached;
    private final String key;

    public AbstractNode(InternalNodeBuilder<?> builder) {
        this.key = Objects.requireNonNull(builder.key());
        this.parent = builder.parent();
        this.attached = builder.config();
    }

    public static ReturnedNode travel(Node node, String... path) {
        return travel(node, 0, path);
    }

    private static ReturnedNode travel(Node node, int index, String... path) {
        Objects.requireNonNull(node);
        if (path.length == index) {
            return new ReturnKnownNodeImpl(node);
        }
        final var targetKey = path[index];
        final var opChildNode = children(node)
                .filter((Node childNode) -> childNode.key().equals(targetKey))
                .findFirst();
        return opChildNode
                .map((Node value) -> travel(value, index + 1, path))
                .orElseGet(() -> new ReturnKnownNodeImpl(null));
    }

    private static Stream<Node> children(Node node) {
        if (node instanceof ContainerNode container) {
            return container.children().stream();
        }
        return (node instanceof CollectionNode collection) ? collection
                .nodes()
                .stream()
                .map(t -> t.plainNode().orElseThrow()) : Stream.empty();
    }

    protected abstract void internalAppendChild(AbstractNode node);

    @Override
    public abstract NodeBuilder.KeyStep<?> toBuilder();

    @Override
    public String key() {
        return this.key;
    }

    @Override
    public ReturnedNode parent() {
        return new ReturnKnownNodeImpl(this.parent);
    }

    @Override
    public ConfigStructure toStructure() {
        return new ConfigStructureImpl(this);
    }

    @SuppressWarnings("PublicMethodNotExposedInInterface")
    public @Nullable Config config() {
        if (this.attached != null) {
            return this.attached;
        }
        return (this.parent == null) ? null : this.parent.config();
    }
}
package com.pixelatedslice.easyconfig.impl.config.node.factory;

import com.pixelatedslice.easyconfig.api.config.node.Node;
import com.pixelatedslice.easyconfig.api.config.node.factory.builder.NodeBuilder;
import com.pixelatedslice.easyconfig.api.validator.null_policy.NullPolicy;
import com.pixelatedslice.easyconfig.impl.config.node.AbstractNode;
import com.pixelatedslice.easyconfig.impl.config.node.InternalNodeBuilder;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NullMarked
public class ReturnKnownNodeImpl implements ReturnedNode {
    private final String toString;
    private final @Nullable Node node;

    public ReturnKnownNodeImpl(@Nullable Node node) {
        this.node = node;

        final StringBuilder sb = new StringBuilder("ReturnKnownNodeImpl{");
        if (node == null) {
            sb.append("key='null', parent=null, fullPath=null");
        } else {
            node.parent();
            sb
                    .append("key='")
                    .append(node.key())
                    .append('\'')
                    .append(", parent=")
                    .append(node.parent().nodeKey().isPresent() ? node.parent().nodeKey().orElse(null) : null)
                    .append(", fullPath=")
                    .append(String.join(",", node.fullPath()));
        }
        this.toString = sb.append('}').toString();
    }

    @Override
    public boolean hasNode() {
        return this.node == null;
    }

    @Override
    public Optional<String> nodeKey() {
        return this.plainNode().map(Node::key);
    }

    @Override
    public Optional<Node> plainNode() {
        return Optional.ofNullable(this.node);
    }

    private <N extends Node> Optional<N> node(Class<N> instanceOf) {
        return this.plainNode().filter(instanceOf::isInstance).map(node -> (N) node);
    }

    @Override
    public Optional<ContainerNode> container() {
        return this.node(ContainerNode.class);
    }

    @Override
    public Optional<CollectionNode> collectionNode() {
        return this.node(CollectionNode.class);
    }

    @Override
    public <T> Optional<ValueNode<T>> value(Class<T> simpleType) {
        return this.value(TypeToken.of(simpleType));
    }

    @Override
    public <T> Optional<ValueNode<T>> value(TypeToken<T> typeToken) {
        //noinspection unchecked
        return this
                .node(ValueNode.class)
                .filter(valueNode -> valueNode.typeToken().equals(typeToken))
                .map(valueNode -> (ValueNode<T>) valueNode);

    }

    @Override
    public Optional<ValueNode<?>> unsafeValue() {
        return this.node(ValueNode.class).map(valueNode -> (ValueNode<?>) valueNode);
    }

    @Override
    public <T> Optional<EnvNode<T>> env(Class<T> simpleType) {
        return this.env(TypeToken.of(simpleType));
    }

    @Override
    public <T> Optional<EnvNode<T>> env(TypeToken<T> typeToken) {
        //noinspection unchecked
        return this
                .node(EnvNode.class)
                .filter(envNode -> envNode.typeToken().equals(typeToken))
                .map(envNode -> (EnvNode<T>) envNode);
    }

    @Override
    public Optional<EnvNode<?>> unsafeEnv() {
        return this.node(EnvNode.class).map(node -> (EnvNode<?>) node);
    }

    @Override
    public String toString() {
        return this.toString;
    }
}
package com.pixelatedslice.easyconfig.impl.config.node.factory.spi;

import com.google.auto.service.AutoService;
import com.google.common.reflect.TypeToken;
import com.pixelatedslice.easyconfig.api.config.node.env.EnvNode;
import com.pixelatedslice.easyconfig.api.config.node.factory.builder.NodeBuilderEnvStep;
import com.pixelatedslice.easyconfig.api.config.node.factory.builder.NodeBuilderGroupStep;
import com.pixelatedslice.easyconfig.api.config.node.factory.builder.NodeBuilderKeySteps;
import com.pixelatedslice.easyconfig.api.config.node.factory.spi.NodeFactoryService;
import com.pixelatedslice.easyconfig.api.config.node.value.ValueNode;
import com.pixelatedslice.easyconfig.api.validator.Validator;
import com.pixelatedslice.easyconfig.impl.config.node.collection.builder.CollectionNodeBuilder;
import com.pixelatedslice.easyconfig.impl.config.node.container.builder.ContainerNodeBuilder;
import com.pixelatedslice.easyconfig.impl.config.node.env.builder.EnvNodeBuilder;
import com.pixelatedslice.easyconfig.impl.config.node.value.builder.ValueNodeBuilder;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public abstract class AbstractGroupNodeBuilder {
    protected final List<InternalNodeBuilder<?>> children = new CopyOnWriteArrayList<>();

    protected void internalChildren(NullPolicy nullPolicy,
            Stream<? extends NodeBuilder.@Nullable BuildStep<?>> nodeStream) {
        final var internalBuilders = nodeStream
                .filter(Objects::nonNull)
                .map(builder -> this.requireInternalNodeBuilder(nullPolicy, builder))
                .collect(Collectors.toSet());

        this.children.addAll(internalBuilders);
    }

    protected void internalBuiltChildren(NullPolicy nullPolicy, Stream<? extends @Nullable Node> nodeStream) {
        final var internalBuilders = nodeStream
                .filter(Objects::nonNull)
                .map(node -> this.requireAbstractNode(nullPolicy, node))
                .map(AbstractNode::toBuilder)
                .map(builder -> this.requireInternalNodeBuilder(nullPolicy, builder))
                .collect(Collectors.toSet());

        this.children.addAll(internalBuilders);
    }

    private AbstractNode requireAbstractNode(NullPolicy nullPolicy, @Nullable Node node) {
        nullPolicy.handle(node);

        if (node instanceof AbstractNode abstractNode) {
            return abstractNode;
        }

        final String actual = (node == null) ? "null" : node.getClass().getName();
        throw new IllegalArgumentException("Expected an AbstractNode implementation, but got: " + actual);
    }

    private InternalNodeBuilder<?> requireInternalNodeBuilder(NullPolicy nullPolicy, NodeBuilder step) {
        nullPolicy.handle(step);

        if (step instanceof InternalNodeBuilder<?> internalNodeBuilder) {
            return internalNodeBuilder;
        }

        final String actual = (step == null) ? "null" : step.getClass().getName();
        throw new IllegalArgumentException("Expected an InternalNodeBuilder, but got: " + actual);
    }
}
package com.pixelatedslice.easyconfig.impl.config.node.factory;

import com.pixelatedslice.easyconfig.api.config.node.env.EnvNode;
import com.pixelatedslice.easyconfig.api.config.node.factory.builder .*;
        import com.pixelatedslice.easyconfig.api.config.node.value.ValueNode;
import org.jspecify.annotations.NullMarked;

@AutoService(NodeFactoryService.class)
@NullMarked
public class NodeFactoryServiceImpl implements NodeFactoryService {
    @Override
    public <T> NodeBuilderKeySteps.Value<T> createValueNodeBuilder(TypeToken<T> typeToken) {
        return new ValueNodeBuilder<>(typeToken);
    }

    @Override
    public <T> ValueNode<T> createValueNode(TypeToken<T> type, String key, @Nullable T defaultValue,
            @Nullable T value) {
        return new ValueNodeBuilder<>(type).key(key).defaultValue(defaultValue).value(value).build();
    }

    @Override
    public <T> NodeBuilderKeySteps.Env<T> createEnvNodeBuilder(TypeToken<T> typeToken) {
        return new EnvNodeBuilder<>(typeToken);
    }

    @Override
    public <T> EnvNode<T> createEnvNode(TypeToken<T> typeToken, String key, String variable,
            @Nullable Function<String, @Nullable T> adapter, @Nullable Validator<T> validator) {
        return new EnvNodeBuilder<>(typeToken)
                .key(key)
                .variable(variable)
                .adapter(adapter)
                .validator(validator)
                .build();
    }

    @Override
    public <T> NodeBuilderEnvStep.AdapterValidatorStep<T> createEnvNodeBuilder(TypeToken<T> typeToken, String key,
            String variable) {
        return new EnvNodeBuilder<>(typeToken).key(key).variable(variable);
    }

    @Override
    public NodeBuilderGroupStep.Container createContainerNodeBuilder(String key) {
        return new ContainerNodeBuilder(key);
    }

    @Override
    public NodeBuilderGroupStep.Collection createCollectionNodeBuilder(String key) {
        return new CollectionNodeBuilder(key);
    }
}
package com.pixelatedslice.easyconfig.impl.config;

import com.pixelatedslice.easyconfig.api.config.BuiltConfig;
import com.pixelatedslice.easyconfig.api.config.node.Node;
import com.pixelatedslice.easyconfig.api.serialization.SerializerRegistry;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public class BuiltConfigImpl implements BuiltConfig {

    private final SerializerRegistry serializers;
    private final Node root;

    public BuiltConfigImpl(Node node) {
        this(node, SerializerRegistry.global());
    }

    public BuiltConfigImpl(Node node, SerializerRegistry serializers) {
        this.root = Objects.requireNonNull(node);
        this.serializers = Objects.requireNonNull(serializers).createChild();
    }


    @Override
    public Node root() {
        return this.root;
    }

    @Override
    public SerializerRegistry serializers() {
        return this.serializers;
    }
}
package com.pixelatedslice.easyconfig.impl.config;

import com.pixelatedslice.easyconfig.api.config.ConfigStructure;
import com.pixelatedslice.easyconfig.api.config.node.Node;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record ConfigStructureImpl(Node root) implements ConfigStructure {

}
package com.pixelatedslice.easyconfig.impl.serializer;

import com.google.common.reflect.TypeToken;
import com.pixelatedslice.easyconfig.api.serialization.Serializer;
import com.pixelatedslice.easyconfig.api.serialization.SerializerRegistry;
import com.pixelatedslice.easyconfig.api.serialization.SerializerRegistryOptions;
import com.pixelatedslice.easyconfig.impl.utils.DistinctByGatherer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

@NullMarked
public class SerializerRegistryImpl implements SerializerRegistry {

    private final @Nullable SerializerRegistryImpl parent;
    private final Map<TypeToken<?>, Serializer<?>> serializers = new ConcurrentHashMap<>();

    public SerializerRegistryImpl(@Nullable SerializerRegistryImpl parent) {
        this.parent = parent;
    }

    @Override
    public SerializerRegistry createChild() {
        return new SerializerRegistryImpl(this);
    }

    @Override
    public Optional<SerializerRegistry> parent() {
        return Optional.ofNullable(this.parent);
    }

    @Override
    public Stream<Serializer<?>> stream() {
        Stream<SerializerRegistryImpl> stream = Stream.of(this);
        var target = this;
        while (target.parent != null) {
            stream = Stream.concat(stream, Stream.of(target.parent));
            target = target.parent;
        }
        return stream
                .flatMap(impl -> impl.serializers.entrySet().stream())
                .gather(new DistinctByGatherer<>(Map.Entry::getKey))
                .map(Map.Entry::getValue);
    }

    @Override
    public <T> Optional<Serializer<T>> serializerFor(TypeToken<T> token) {
        @SuppressWarnings("unchecked")
        final var result = (Serializer<T>) this.serializers.get(Objects.requireNonNull(token));
        return ((result == null) && (this.parent != null))
                ? this.parent.serializerFor(token)
                : Optional.ofNullable(result);
    }

    @Override
    public SerializerRegistry register(Consumer<SerializerRegistryOptions> options,
            Iterator<Serializer<?>> serializers) {
        final var optionsImpl = new SerializerRegistryOptionsImpl();
        options.accept(optionsImpl);
        while (serializers.hasNext()) {
            var serializer = serializers.next();
            final var type = serializer.type();
            final var originalSerializer = this.serializers.get(type);
            if (originalSerializer != null) {
                serializer = optionsImpl.duplicateRegisterStyle().action().apply(originalSerializer, serializer);
            }
            this.serializers.put(type, serializer);
        }
        return this;
    }
}
package com.pixelatedslice.easyconfig.impl.serializer;

import com.pixelatedslice.easyconfig.api.serialization.SerializerRegistryOptions;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
public class SerializerRegistryOptionsImpl implements SerializerRegistryOptions {

    private SerializerRegistryOptions.DuplicateRegisterStyle duplicateRegistryStyle = DuplicateRegisterStyle.THROW;

    @Override
    public SerializerRegistryOptions duplicateRegisterStyle(DuplicateRegisterStyle style) {
        this.duplicateRegistryStyle = Objects.requireNonNull(style);
        return this;
    }

    @Override
    public SerializerRegistryOptions.DuplicateRegisterStyle duplicateRegisterStyle() {
        return this.duplicateRegistryStyle;
    }
}
package com.pixelatedslice.easyconfig.impl.utils;

import org.jspecify.annotations.NullMarked;


import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Gatherer;

@NullMarked
public class DistinctByGatherer<V, K> implements Gatherer<V, LinkedBlockingQueue<V>, V> {

    private final Function<V, K> by;

    public DistinctByGatherer(Function<V, K> by) {
        this.by = Objects.requireNonNull(by);
    }

    @Override
    public Supplier<LinkedBlockingQueue<V>> initializer() {
        return LinkedBlockingQueue::new;
    }

    @Override
    public Integrator<LinkedBlockingQueue<V>, V, V> integrator() {
        return (vs, v, downstream) -> {
            if (this.check(vs, v)) {
                vs.add(v);
            }
            return true;
        };
    }

    private boolean check(Collection<V> compare, V checking) {
        final var incomingBy = this.by.apply(checking);
        return compare.stream().map(this.by).noneMatch(value -> value.equals(incomingBy));
    }

    @Override
    public BinaryOperator<LinkedBlockingQueue<V>> combiner() {
        return (vs, vs2) -> {
            final var vs1Filter = vs2.stream().filter(value -> this.check(vs, value)).toList();
            vs.addAll(vs1Filter);
            return vs;
        };
    }

    @Override
    public BiConsumer<LinkedBlockingQueue<V>, Downstream<? super V>> finisher() {
        return (vs, downstream) -> {
            vs.forEach(downstream::push);
        };
    }
}
import com.pixelatedslice.easyconfig.api.config.node.factory.spi.NodeFactoryService;
import com.pixelatedslice.easyconfig.impl.config.node.factory.spi.NodeFactoryServiceImpl;

open module com.pixelatedslice.easyconfig.impl {
    requires com.google.common;
    requires org.jspecify;
    requires com.pixelatedslice.easyconfig.api;
    requires com.google.auto.service;
    requires com.google.errorprone.annotations;

    provides NodeFactoryService with NodeFactoryServiceImpl;
}
package com.pixelatedslice.easyconfig.impl.test.config.node;

        import com.pixelatedslice.easyconfig.api.config.node.factory.Nodes;
        import org.jspecify.annotations.NullMarked;
        import org.junit.jupiter.api.Assertions;
        import org.junit.jupiter.api.Test;

        @NullMarked public class ContainerNodeBuilderTests{

        @Test public void NodeBuilder_should_build_container_node_with_key(){
        //ARRANGE
        final String key="My Key";

        //ACT
        // Old: final var node = this.builder().key(key).build();
        final var node=Nodes.container(key).build();

        //ASSERT
        Assertions.assertEquals(key,node.key());
        }

        @SuppressWarnings("DataFlowIssue")
        @Test public void NodeBuilder_should_throw_exception_with_null_key(){
        //ARRANGE

        //ACT - ASSERT
        // Old: Assertions.assertThrows(NullPointerException.class, () ->
        //        this.builder().key(null).build());

        Assertions.assertThrows(NullPointerException.class,()->Nodes.container(null).build());
        }

        @Test public void NodeBuilder_should_append_with_provided_key(){
        //ARRANGE
        final var key="My first key";
        final var secondKey="My second key";

        //ACT
        // Old: final var result = this.builder().key(key).append(secondKey).complete().build();
        final var result=Nodes.container(key).children(Nodes.container(secondKey)).build();

        //ASSERT
        Assertions.assertEquals(key,result.key());
        final var opNode=result.containerNode(secondKey);
        Assertions.assertTrue(opNode.isPresent());
        Assertions.assertEquals(secondKey,opNode.get().key());
        }

        @Test public void NodeBuilder_should_append_child_with_provided_key(){
        //ARRANGE
        final var key="My first key";
        final var secondKey="My second key";
        final var thirdKey="My third key";

        //ACT
        // Old: final var result = this.builder().key(key).append(secondKey).append(thirdKey).complete().complete()
        // .build();
        final var result=Nodes.container(key).children(
        Nodes.container(secondKey).children(
        Nodes.container(thirdKey)
        )
        ).build();

        //ASSERT
        Assertions.assertEquals(key,result.key());
        final var opNode=result.containerNode(secondKey);
        Assertions.assertTrue(opNode.isPresent());
        Assertions.assertEquals(secondKey,opNode.get().key());
        final var opSecondNode=opNode.get().containerNode(thirdKey);
        Assertions.assertTrue(opSecondNode.isPresent());
        Assertions.assertEquals(thirdKey,opSecondNode.get().key());
        }
        }
        package com.pixelatedslice.easyconfig.impl.test.config.node.envKey;

        import com.pixelatedslice.easyconfig.api.config.node.env.EnvKeys;
        import org.jspecify.annotations.NullMarked;
        import org.junit.jupiter.api.Assertions;
        import org.junit.jupiter.api.Test;

        import java.io.File;
        import java.io.IOException;
        import java.nio.file.Path;

        @NullMarked public class HomeEnvKeyTests{

        @Test public void home_has_correct_key(){
        //ARRANGE
        final var key="HOME";

        //ACT
        final var result=EnvKeys.HOME.key();

        //ASSERT
        Assertions.assertEquals(key,result);
        }

        @Test public void home_splits_valid_result_into_Lang_Result()throws IOException{
        //ARRANGE
        final Path expected=new File(".").toPath().toRealPath();
        final String input=expected.toString();

        //ACT
        final var result=EnvKeys.HOME.adapter().apply(input);

        //ASSERT
        Assertions.assertNotNull(result);
        Assertions.assertEquals(expected,result);
        }

        @Test public void home_returns_null_when_invalid(){
        //ARRANGE
        final String input="!";

        //ACT
        final var result=EnvKeys.HOME.adapter().apply(input);

        //ASSERT
        Assertions.assertNull(result);
        }
        }
        package com.pixelatedslice.easyconfig.impl.test.config.node.envKey;

        import com.pixelatedslice.easyconfig.api.config.node.env.EnvKeys;
        import org.jspecify.annotations.NullMarked;
        import org.junit.jupiter.api.Assertions;
        import org.junit.jupiter.api.Test;

        @NullMarked public class LangEnvKeyTests{

        @Test public void lang_has_correct_key(){
        //ARRANGE
        final var key="LANG";

        //ACT
        final var result=EnvKeys.LANG.key();

        //ASSERT
        Assertions.assertEquals(key,result);
        }

        @Test public void lang_splits_valid_result_into_Lang_Result(){
        //ARRANGE
        final String input="en_GB.UTF-8";

        //ACT
        final var result=EnvKeys.LANG.adapter().apply(input);

        //ASSERT
        Assertions.assertNotNull(result);
        Assertions.assertEquals("en_gb",result.language().toString());
        Assertions.assertEquals("UTF-8",result.encoding());
        }

        @Test public void lang_returns_null_when_invalid_without_dot_input(){
        //ARRANGE
        final String input="invalid";

        //ACT
        final var result=EnvKeys.LANG.adapter().apply(input);

        //ASSERT
        Assertions.assertNull(result);
        }
        }
        package com.pixelatedslice.easyconfig.impl.test.config.node.envKey;

        import com.pixelatedslice.easyconfig.api.config.node.env.EnvKeys;
        import org.jspecify.annotations.NullMarked;
        import org.junit.jupiter.api.Assertions;
        import org.junit.jupiter.api.Test;

        @NullMarked public class UserEnvKeyTests{

        @Test public void lang_has_correct_key(){
        //ARRANGE
        final var key="USER";

        //ACT
        final var result=EnvKeys.USER.key();

        //ASSERT
        Assertions.assertEquals(key,result);
        }

        @Test public void lang_splits_valid_result_into_Lang_Result(){
        //ARRANGE
        final String input="MyUser";

        //ACT
        final var result=EnvKeys.USER.adapter().apply(input);

        //ASSERT
        Assertions.assertNotNull(result);
        Assertions.assertEquals(input,result);
        }
        }
        package com.pixelatedslice.easyconfig.impl.test.config.node.envKey;

        import com.pixelatedslice.easyconfig.api.config.node.env.EnvKeys;
        import com.pixelatedslice.easyconfig.impl.test.testUtils.CollectionAssert;
        import org.jspecify.annotations.NullMarked;
        import org.junit.jupiter.api.Assertions;
        import org.junit.jupiter.api.Test;

        import java.io.File;
        import java.io.IOException;
        import java.nio.file.Path;
        import java.util.List;

        @NullMarked public class PathsEnvKeyTests{

        @Test public void paths_has_correct_key(){
        //ARRANGE
        final var key="PATH";

        //ACT
        final var result=EnvKeys.PATHS.key();

        //ASSERT
        Assertions.assertEquals(key,result);
        }

        @Test public void paths_splits_valid_result_into_Lang_Result()throws IOException{
        //ARRANGE
        final String file1=new File("src/main").getAbsolutePath();
        final String file2=new File("src/test").getAbsolutePath();

        final String input=file1+";"+file2;

        //ACT
        final var result=EnvKeys.PATHS.adapter().apply(input);

        //ASSERT
        Assertions.assertNotNull(result);
        CollectionAssert.isEqualTo(List.of(Path.of(file1),Path.of(file2)),result,false);
        }

        @Test public void paths_returns_null_when_invalid(){
        //ARRANGE
        final String input="!";

        //ACT
        final var result=EnvKeys.PATHS.adapter().apply(input);

        //ASSERT
        Assertions.assertNull(result);
        }
        }
        package com.pixelatedslice.easyconfig.impl.test.config.node.envKey;

        import com.pixelatedslice.easyconfig.api.config.node.env.EnvKeys;
        import org.junit.jupiter.api.Assertions;
        import org.junit.jupiter.api.Test;

        import java.io.File;
        import java.io.IOException;
        import java.nio.file.Path;

        public class ShellEnvKeyTests{

        @Test public void editor_has_correct_key(){
        //ARRANGE
        var key="SHELL";

        //ACT
        var result=EnvKeys.SHELL.key();

        //ASSERT
        Assertions.assertEquals(key,result);
        }

        @Test public void editor_splits_valid_result_into_Lang_Result()throws IOException{
        //ARRANGE
        Path expected=new File(".").toPath().toRealPath();
        String input=expected.toString();

        //ACT
        var result=EnvKeys.SHELL.adapter().apply(input);

        //ASSERT
        Assertions.assertNotNull(result);
        Assertions.assertEquals(expected,result);
        }

        @Test public void editor_returns_null_when_invalid(){
        //ARRANGE
        String input="!";

        //ACT
        var result=EnvKeys.SHELL.adapter().apply(input);

        //ASSERT
        Assertions.assertNull(result);
        }
        }
        package com.pixelatedslice.easyconfig.impl.test.config.node.envKey;

        import com.pixelatedslice.easyconfig.api.config.node.env.EnvKeys;
        import org.jspecify.annotations.NullMarked;
        import org.junit.jupiter.api.Assertions;
        import org.junit.jupiter.api.Test;

        import java.io.File;
        import java.io.IOException;
        import java.nio.file.Path;

        @NullMarked public class EditorEnvKeyTests{

        @Test public void editor_has_correct_key(){
        //ARRANGE
        final var key="EDITOR";

        //ACT
        final var result=EnvKeys.EDITOR.key();

        //ASSERT
        Assertions.assertEquals(key,result);
        }

        @Test public void editor_splits_valid_result_into_Lang_Result()throws IOException{
        //ARRANGE
        final Path expected=new File(".").toPath().toRealPath();
        final String input=expected.toString();

        //ACT
        final var result=EnvKeys.EDITOR.adapter().apply(input);

        //ASSERT
        Assertions.assertNotNull(result);
        Assertions.assertEquals(expected,result);
        }

        @Test public void editor_returns_null_when_invalid(){
        //ARRANGE
        final String input="!";

        //ACT
        final var result=EnvKeys.EDITOR.adapter().apply(input);

        //ASSERT
        Assertions.assertNull(result);
        }
        }
        package com.pixelatedslice.easyconfig.impl.test.config.node.envKey;

        import com.pixelatedslice.easyconfig.api.config.node.env.EnvKeys;
        import org.jspecify.annotations.NullMarked;
        import org.junit.jupiter.api.Assertions;
        import org.junit.jupiter.api.Test;

        @NullMarked public class DisplayEnvKeyTests{

        @Test public void display_has_correct_key(){
        //ARRANGE
        final var key="DISPLAY";

        //ACT
        final var result=EnvKeys.DISPLAY.key();

        //ASSERT
        Assertions.assertEquals(key,result);
        }

        @Test public void lang_parses_correctly(){
        //ARRANGE
        final String input=":0";

        //ACT
        final var result=EnvKeys.DISPLAY.adapter().apply(input);

        //ASSERT
        Assertions.assertNotNull(result);
        Assertions.assertEquals(0,result);
        }

        @Test public void lang_returns_null_when_invalid_without_dot_input(){
        //ARRANGE
        final String input="invalid";

        //ACT
        final var result=EnvKeys.DISPLAY.adapter().apply(input);

        //ASSERT
        Assertions.assertNull(result);
        }
        }
        package com.pixelatedslice.easyconfig.impl.test.config.node.envKey;

        import com.pixelatedslice.easyconfig.api.config.node.env.EnvKeys;
        import org.jspecify.annotations.NullMarked;
        import org.junit.jupiter.api.Assertions;
        import org.junit.jupiter.api.Test;

        @NullMarked public class DebuggerIdEnvKeyTests{

        @Test public void lang_has_correct_key(){
        //ARRANGE
        final var key="DEBUGGER_ID";

        //ACT
        final var result=EnvKeys.DEBUGGER_ID.key();

        //ASSERT
        Assertions.assertEquals(key,result);
        }

        @Test public void lang_splits_valid_result_into_Lang_Result(){
        //ARRANGE
        final String input="MyDebugger";

        //ACT
        final var result=EnvKeys.DEBUGGER_ID.adapter().apply(input);

        //ASSERT
        Assertions.assertNotNull(result);
        Assertions.assertEquals(input,result);
        }
        }
        package com.pixelatedslice.easyconfig.impl.test.config.node.envKey;

        import com.pixelatedslice.easyconfig.api.config.node.env.EnvKeys;
        import org.jspecify.annotations.NullMarked;
        import org.junit.jupiter.api.Assertions;
        import org.junit.jupiter.api.Test;

        @NullMarked public class DebuggerEnabledEnvKeyTests{

        @Test public void debugger_enabled_has_correct_key(){
        //ARRANGE
        final var key="DEBUGGER_ENABLED";

        //ACT
        final var result=EnvKeys.DEBUGGING_ENABLED.key();

        //ASSERT
        Assertions.assertEquals(key,result);
        }

        @Test public void lang_parses_correctly(){
        //ARRANGE
        final String input="true";

        //ACT
        final var result=EnvKeys.DEBUGGING_ENABLED.adapter().apply(input);

        //ASSERT
        Assertions.assertNotNull(result);
        Assertions.assertEquals(true,result);
        }

        @Test public void lang_returns_false_when_invalid(){
        //ARRANGE
        final String input="invalid";

        //ACT
        final var result=EnvKeys.DEBUGGING_ENABLED.adapter().apply(input);

        //ASSERT
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result);
        }
        }
        package com.pixelatedslice.easyconfig.impl.test.config.node;

        import com.google.common.reflect.TypeToken;
        import com.pixelatedslice.easyconfig.api.config.node.factory.Nodes;
        import org.jspecify.annotations.NullMarked;
        import org.junit.jupiter.api.Assertions;
        import org.junit.jupiter.api.Test;

        import java.util.function.Function;

        @NullMarked public class EnvBuilderTests{
        @Test public void NodeBuilder_can_build_node(){
        //ARRANGE
        final var key="First key";
        final var typeToken=TypeToken.of(String.class);
        final var envKey="envKey";
        final Function<String,String>adapter=t->t;

        //ACT
        final var result=Nodes.env(typeToken).key(key).variable(envKey).adapter(adapter).build();

        //ASSERT
        Assertions.assertEquals(key,result.key());
        Assertions.assertEquals(typeToken,result.typeToken());
        Assertions.assertEquals(envKey,result.envKey());
        Assertions.assertEquals(adapter,result.adapter());
        }
        }
        package com.pixelatedslice.easyconfig.impl.test.config.node;

        import com.google.common.reflect.TypeToken;
        import com.pixelatedslice.easyconfig.api.config.node.container.ContainerNode;
        import com.pixelatedslice.easyconfig.api.config.node.factory.Nodes;
        import com.pixelatedslice.easyconfig.api.config.node.value.ValueNode;
        import com.pixelatedslice.easyconfig.api.exception.ValidationException;
        import com.pixelatedslice.easyconfig.api.validator.Validator;
        import com.pixelatedslice.easyconfig.api.validator.ValidatorContext;
        import com.pixelatedslice.easyconfig.api.validator.option.ValidationOptions;
        import org.jspecify.annotations.NullMarked;
        import org.junit.jupiter.api.Assertions;
        import org.junit.jupiter.api.Test;

        import java.util.Optional;
        import java.util.regex.Pattern;

        @NullMarked public class ValueNodeBuilderTests{
        @Test public void NodeBuilder_should_build_value_node_with_expected(){
        //ARRANGE
        final String key="My Key";
        final TypeToken<String>type=TypeToken.of(String.class);
        final var defaultValue="default value";
        final var value="value";

        //ACT
        final var node=Nodes.value(String.class).key(key).defaultValue(defaultValue).value(value).build();

        //ASSERT
        Assertions.assertEquals(key,node.key());
        Assertions.assertEquals(type,node.typeToken());
        Assertions.assertEquals(defaultValue,node.defaultValue().orElse(null));
        Assertions.assertEquals(value,node.value().orElse(null));
        }

        @Test public void NodeBuilder_should_throw_exception_with_null_key(){
        //ARRANGE

        //ACT - ASSERT
        //noinspection DataFlowIssue
        Assertions.assertThrows(NullPointerException.class,()->Nodes.value(String.class).key(null).build());
        }

        @Test public void NodeBuilder_should_throw_exception_with_null_typetoken(){
        //ARRANGE
        final var key="My Key";

        System.getenv("");

        //ACT - ASSERT
        // Old: Assertions.assertThrows(NullPointerException.class, () ->
        //         this.builder()
        //                 .key(key)
        //                 .of((TypeToken<?>) null)
        //                 .build());
        //noinspection DataFlowIssue
        Assertions.assertThrows(NullPointerException.class,()->Nodes.value((TypeToken<?>)null).key(key));
        }

        @Test public void NodeBuilder_should_append_value_with_provided_key(){
        //ARRANGE
        final var key="My first key";
        final var secondKey="My second key";
        final var type=TypeToken.of(String.class);

        //ACT
        // final var builder = this.builder().key(key).append(secondKey).of(String.class);
        // final OldNodeBuilder.ContainerFinalStep.Original originalBuilder = builder.complete();
        final ContainerNode result=Nodes.container(key).children(Nodes.value(String.class).key(secondKey)).build();

        //ASSERT
        Assertions.assertEquals(key,result.key());

        final Optional<ValueNode<String>>opNode=result.valueNode(String.class,secondKey);
        Assertions.assertTrue(opNode.isPresent());
        Assertions.assertEquals(secondKey,opNode.get().key());
        Assertions.assertInstanceOf(ValueNode.class,opNode.get());
        Assertions.assertEquals(type,opNode.get().typeToken());
        }

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        @Test public void NodeBuilder_should_update_value(){
        //ARRANGE
        final var key="My first key";
        final var oldValue="Old value";
        final var newValue="Old value";
        final var type=TypeToken.of(String.class);

        //ACT
        final var result=Nodes.value(type).key(key).value(oldValue).build();
        final var beforeUpdating=result.value().get();

        try(var editable=result.editable()){
        editable.setValue(newValue);
        }

        final var afterUpdating=result.value().get();

        //ASSERT
        Assertions.assertEquals(key,result.key());
        Assertions.assertEquals(type,result.typeToken());
        Assertions.assertEquals(oldValue,beforeUpdating);
        Assertions.assertEquals(newValue,afterUpdating);
        }

        @SuppressWarnings("TypeMayBeWeakened")
        @Test public void NodeBuilder_validate_should_throw_and_be_empty(){
        // ARRANGE
        final var key="Validation Test Node";
        final var type=TypeToken.of(String.class);
        final var value="superman.123";

        final var emailRegex=Pattern.compile("^\\S+@\\S+\\.\\S+$");
        final Validator<String>validator=(String validationValue,ValidatorContext context)->{
        if(!emailRegex.matcher(validationValue).matches()){
        context.error("Invalid email address");
        }
        };

        // ACT
        final var node=Nodes.value(type).key(key).value(value).validator(validator).build();

        // ASSERT
        Assertions.assertThrows(ValidationException.class,node::value);
        Assertions.assertEquals(Optional.empty(),node.value(ValidationOptions.returnEmpty()));
        }
        }
        package com.pixelatedslice.easyconfig.impl.test.config.node.specific;

        import com.google.common.reflect.TypeToken;
        import com.pixelatedslice.easyconfig.api.config.node.env.EnvAdapter;
        import com.pixelatedslice.easyconfig.impl.config.node.env.EnvNodeImpl;
        import com.pixelatedslice.easyconfig.impl.config.node.env.builder.EnvNodeBuilder;
        import org.jspecify.annotations.NullMarked;
        import org.junit.jupiter.api.Assertions;
        import org.junit.jupiter.api.Test;

        @NullMarked public class EnvNodeTests{

        @Test public void EnvNode_fails_when_creating_without_env_adapter(){
        //ARRANGE
        final var key="key";
        final var token=TypeToken.of(String.class);
        final String envKey="LANG";
        final var internalBuilder=new EnvNodeBuilder<>(token,key,envKey);

        //ACT - ASSERT
        Assertions.assertThrows(NullPointerException.class,()->new EnvNodeImpl<>(internalBuilder));
        }

        @Test public void EnvNode_valid_when_creating(){
        //ARRANGE
        final String key="key";
        final var token=TypeToken.of(String.class);
        final var envKey="LANG";
        final EnvAdapter<String>adapter=(String t)->t;
        final var internalBuilder=new EnvNodeBuilder<>(token,key,envKey).adapter(adapter);

        //ACT
        final var result=new EnvNodeImpl<>(internalBuilder);

        //ASSERT
        Assertions.assertEquals(key,result.key());
        Assertions.assertEquals(envKey,result.envKey());
        Assertions.assertEquals(token,result.typeToken());
        Assertions.assertEquals(System.getenv(envKey),result.value().orElse(""));
        }

        @Test public void EnvNode_to_builder(){
        //ARRANGE
        final String key="key";
        final var token=TypeToken.of(String.class);
        final var envKey="LANG";
        final EnvAdapter<String>adapter=t->t;
        final var internalBuilder=new EnvNodeBuilder<>(token,key,envKey).adapter(adapter);

        final var node=new EnvNodeImpl<>(internalBuilder);

        //ACT
        final var result=node.toBuilder();

        //ASSERT
        Assertions.assertInstanceOf(EnvNodeBuilder.class,result);
        final var castResult=result;
        Assertions.assertEquals(key,castResult.key());
        Assertions.assertEquals(envKey,castResult.envKey());
        Assertions.assertEquals(token,castResult.type());
        Assertions.assertEquals(adapter,castResult.adapter());
        }
        }
        package com.pixelatedslice.easyconfig.impl.test.config.node.specific;

        import com.google.common.reflect.TypeToken;
        import com.pixelatedslice.easyconfig.api.validator.option.ValidationOptions;
        import com.pixelatedslice.easyconfig.impl.config.node.value.ValueNodeImpl;
        import com.pixelatedslice.easyconfig.impl.config.node.value.builder.ValueNodeBuilder;
        import org.jspecify.annotations.NullMarked;
        import org.junit.jupiter.api.Assertions;
        import org.junit.jupiter.api.Test;

        import java.util.Optional;

        @NullMarked public class ValueNodeTests{

        @Test public void ValueNode_can_create(){
        // Arrange
        final var builder=new ValueNodeBuilder<>(TypeToken.of(String.class),"key",null,null);

        // Act
        final var node=new ValueNodeImpl<>(builder);

        // Assert
        Assertions.assertEquals(TypeToken.of(String.class),node.typeToken());
        Assertions.assertEquals("key",node.key());
        }

        @Test public void ValueNode_valueOrDefault_can_receive_default_when_no_value_present(){
        // Arrange
        final var builder=new ValueNodeBuilder<>(TypeToken.of(String.class),"key",null,null);
        builder.defaultValue("default");

        // Act
        final var node=new ValueNodeImpl<>(builder);

        // Assert
        Assertions.assertEquals(TypeToken.of(String.class),node.typeToken());
        Assertions.assertEquals("key",node.key());
        Assertions.assertEquals("default",node.valueOrDefault().orElseThrow());
        Assertions.assertEquals("default",node.defaultValue().orElseThrow());
        Assertions.assertEquals(Optional.empty(),node.value());
        }

        @Test public void ValueNode_valueOrDefault_can_receive_value_when_no_default_present(){
        // Arrange
        final var builder=new ValueNodeBuilder<>(TypeToken.of(String.class),"key",null,null);
        builder.value("value");

        // Act
        final var node=new ValueNodeImpl<>(builder);

        // Assert
        Assertions.assertEquals(TypeToken.of(String.class),node.typeToken());
        Assertions.assertEquals("key",node.key());
        Assertions.assertEquals("value",node.valueOrDefault().orElseThrow());
        Assertions.assertEquals("value",node.value().orElseThrow());
        Assertions.assertEquals(Optional.empty(),node.defaultValue());
        }

        @Test public void ValueNode_valueOrDefault_can_receive_value_when_both_default_and_value_are_present(){
        // Arrange
        final var builder=new ValueNodeBuilder<>(TypeToken.of(String.class),"key",null,null);
        builder.value("value");
        builder.defaultValue("default");

        // Act
        final var node=new ValueNodeImpl<>(builder);

        // Assert
        Assertions.assertEquals(TypeToken.of(String.class),node.typeToken());
        Assertions.assertEquals("key",node.key());
        Assertions.assertEquals("value",node.valueOrDefault().orElseThrow());
        Assertions.assertEquals("default",node.defaultValue().orElseThrow());
        Assertions.assertEquals("value",node.value().orElseThrow());
        }

        @Test public void ValueNode_value_validate_fails(){
        // Arrange
        final var builder=new ValueNodeBuilder<>(TypeToken.of(String.class),"key",null,null);
        builder.value("value");
        builder.validator((_,context)->context.error("Big error"));
        final var node=new ValueNodeImpl<>(builder);

        // Act
        final var result=node.value(ValidationOptions.returnEmpty());

        // Assert
        Assertions.assertEquals(Optional.empty(),result);
        }

        @Test public void ValueNode_editable_can_edit_value(){
        // Arrange
        final var builder=new ValueNodeBuilder<>(TypeToken.of(String.class),"key",null,null);
        builder.value("Old value");
        final var node=new ValueNodeImpl<>(builder);

        // Act
        try(var editable=node.editable()){
        editable.setValue("New value");
        }

        // Assert
        Assertions.assertEquals("New value",node.value().orElseThrow());
        }

        @Test public void ValueNode_toBuilder_retains_values(){
        // Arrange
        final var builder=new ValueNodeBuilder<>(TypeToken.of(String.class),"key",null,null);
        builder.value("value");
        builder.defaultValue("default value");
        final var node=new ValueNodeImpl<>(builder);

        // Act
        final var newBuilder=node.toBuilder();

        // Assert
        Assertions.assertEquals("value",newBuilder.value());
        Assertions.assertEquals("default value",newBuilder.defaultValue());
        Assertions.assertEquals("key",newBuilder.key());
        Assertions.assertEquals(TypeToken.of(String.class),newBuilder.type());
        }
        }
        package com.pixelatedslice.easyconfig.impl.test.config.node.specific;

        import com.pixelatedslice.easyconfig.api.config.node.Node;
        import com.pixelatedslice.easyconfig.api.config.node.factory.CommonTypes;
        import com.pixelatedslice.easyconfig.api.config.node.factory.Nodes;
        import com.pixelatedslice.easyconfig.impl.config.node.container.ContainerNodeImpl;
        import com.pixelatedslice.easyconfig.impl.config.node.container.builder.ContainerNodeBuilder;
        import com.pixelatedslice.easyconfig.impl.test.testUtils.CollectionAssert;
        import org.jspecify.annotations.NullMarked;
        import org.junit.jupiter.api.Assertions;
        import org.junit.jupiter.api.Test;

        import java.util.Collections;
        import java.util.List;

        @NullMarked public class ContainerNodeTests{

        @Test public void ContainerNode_can_maintain_values(){
        //ARRANGE
        final var key="key";
        final var internalBuilder=new ContainerNodeBuilder(key);

        //ACT
        final var result=new ContainerNodeImpl(internalBuilder);

        //ASSERT
        Assertions.assertEquals(key,result.key());
        Assertions.assertTrue(result.isRootNode(),"is not root node");
        }

        @Test public void ContainerNode_to_builder(){
        //ARRANGE
        final var key="key";
        final var internalBuilder=new ContainerNodeBuilder(key);

        final Node node=new ContainerNodeImpl(internalBuilder);

        //ACT
        final var result=node.toBuilder();

        //ASSERT
        Assertions.assertInstanceOf(ContainerNodeBuilder.class,result);
        final var castResult=(ContainerNodeBuilder)result;
        Assertions.assertEquals(key,castResult.key());
        }

        @Test public void ContainerNode_editable_addNode(){
        //ARRANGE
        final var key="original";
        final var originalNode=new ContainerNodeBuilder(key).build();

        final var toAdd=new ContainerNodeBuilder("adding").build();

        //ACT
        try(var editable=originalNode.editable()){
        editable.addNodes(toAdd);
        }

        //ASSERT
        CollectionAssert.isEqualTo(List.of(toAdd),originalNode.children(),true);
        }

        @Test public void ContainerNode_editable_clearNodes(){
        //ARRANGE
        final var key="original";
        final var originalNode=Nodes
        .container(key)
        .builtChildren(Nodes.emptyValue(CommonTypes.STRING,"added"))
        .build();

        //ACT
        try(var editable=originalNode.editable()){
        editable.clearNodes();
        }

        //ASSERT
        CollectionAssert.isEqualTo(Collections.emptyList(),originalNode.children(),true);
        }

        @Test public void ContainerNode_editable_removeNode(){
        //ARRANGE
        final var key="original";
        final var originalNode=Nodes
        .container(key)
        .builtChildren(
        Nodes.emptyValue(CommonTypes.STRING,"added"),
        Nodes.emptyValue(CommonTypes.STRING,"second")
        ).build();
        final var toRemain=originalNode.children().getLast();
        final var toRemove=originalNode.children().getFirst();

        //ACT
        try(var editable=originalNode.editable()){
        editable.removeNodes(toRemove);
        }

        //ASSERT
        CollectionAssert.isEqualTo(List.of(toRemain),originalNode.children(),true);
        }

        @Test public void ContainerNode_editable_setNode(){
        //ARRANGE
        final var key="original";
        final var originalNode=Nodes
        .container(key)
        .builtChildren(Nodes.emptyValue(CommonTypes.STRING,"added"),
        Nodes.emptyValue(CommonTypes.STRING,"second"))
        .build();

        final var toSet=new ContainerNodeBuilder("set").build();

        //ACT
        try(var editable=originalNode.editable()){
        editable.setNodes(List.of(toSet));
        }

        //ASSERT
        CollectionAssert.isEqualTo(List.of(toSet),originalNode.children(),true);
        }
        }
        package com.pixelatedslice.easyconfig.impl.test.config.node.specific;

        import com.pixelatedslice.easyconfig.impl.config.node.collection.builder.CollectionNodeBuilder;
        import org.jspecify.annotations.NullMarked;
        import org.junit.jupiter.api.Assertions;
        import org.junit.jupiter.api.Test;

        @NullMarked public class CollectionNodeTests{

        @Test public void CollectionNode_can_build_correctly(){
        //ARRANGE
        final var builder=new CollectionNodeBuilder("key");

        //ACT
        final var result=builder.build();

        //ASSERT
        final var children=result.stream().map(r->r.container().orElseThrow()).toList();
        Assertions.assertEquals(0,children.size());
        }

        @Test public void CollectionNode_toBuilder(){
        //ARRANGE
        final var node=new CollectionNodeBuilder("key").build();

        //ACT
        final var toBuilder=node.toBuilder();

        //ASSERT
        Assertions.assertEquals("key",toBuilder.key());
        Assertions.assertEquals(0,toBuilder.children().size());
        }
        }
        package com.pixelatedslice.easyconfig.impl.test.config.node;

        import com.pixelatedslice.easyconfig.api.config.node.factory.Nodes;
        import org.jspecify.annotations.NullMarked;
        import org.junit.jupiter.api.Assertions;
        import org.junit.jupiter.api.Test;

        @NullMarked public class CollectionNodeBuilderTests{

        @Test public void NodeBuilder_collection_builds_empty(){
        //ARRANGE
        final String key="First key";

        //ACT
        // Old: final var result = this.builder().key(key).collection().build();
        final var result=Nodes.collection(key).build();

        //ASSERT
        Assertions.assertEquals(key,result.key());
        }

        @Test public void NodeBuilder_collection_builds_with_single(){
        //ARRANGE
        final String key="First key";

        //ACT
        // Old: final var result = this.builder().key(key).collection().append().complete().build();
        final var result=Nodes.collection(key).build();

        //ASSERT
        Assertions.assertEquals(key,result.key());
        final var opChildNode=result.atIndex(0).plainNode();
        // Old: Assertions.assertTrue(opChildNode.isPresent());
        // Old: Assertions.assertEquals("index_0", opChildNode.get().key());
        Assertions.assertFalse(opChildNode.isPresent());
        }

        @Test public void NodeBuilder_collection_builds_with_single_with_container(){
        //ARRANGE
        final String key="First key";
        final String thirdKey="Third key";

        //ACT
        // Old: final var result = this.builder().key(key).collection().append().append(thirdKey).complete().complete
        // ().build();
        final var result=Nodes.collection(key).children(
        Nodes.container("index_0").children(
        Nodes.container(thirdKey)
        )
        ).build();

        //ASSERT
        Assertions.assertEquals(key,result.key());
        final var opChildNode=result.atIndex(0).container();
        Assertions.assertTrue(opChildNode.isPresent());
        Assertions.assertEquals("index_0",opChildNode.get().key());
        final var opSecondChildNode=opChildNode.get().containerNode(thirdKey);
        Assertions.assertTrue(opSecondChildNode.isPresent());
        Assertions.assertEquals(thirdKey,opSecondChildNode.get().key());
        }

        @Test public void NodeBuilder_collection_builds_with_single_with_collection_with_container(){
        //ARRANGE
        final String key="First key";
        final String fourKey="Four key";

        //ACT
        // Old: final var result = this.builder()
        //         .key(key)
        //         .collection()
        //         .append()
        //           .collection()
        //             .append()
        //              .append(fourKey)
        //             .complete()
        //           .complete()
        //         .complete()
        //         .build();

        final var firstNode=Nodes.collection(key);
        final var secondNode=Nodes.collection("Second key");
        final var thirdNode=Nodes.container("Third key");
        final var fourthNode=Nodes.container(fourKey);

        final var result=firstNode.children(
        secondNode.children(
        thirdNode.children(
        fourthNode
        )
        )
        ).build();

        System.out.println("result = "+result);

        //ASSERT
        Assertions.assertEquals(key,result.key());
        final var opSecondCollection=result.atIndex(0).collectionNode();
        Assertions.assertTrue(opSecondCollection.isPresent());
        Assertions.assertEquals("Second key",opSecondCollection.get().key());
        System.out.println("opSecondCollection.get() = "+opSecondCollection.get());
        final var opThirdChildNode=opSecondCollection.get().atIndex(0).container();
        Assertions.assertTrue(opThirdChildNode.isPresent()); //index container
        System.out.println("opThirdChildNode.get() = "+opThirdChildNode.get());
        final var opFourChildNode=opThirdChildNode.get().containerNode(fourKey);
        Assertions.assertTrue(opFourChildNode.isPresent());
        }
        }
        package com.pixelatedslice.easyconfig.impl.test.serializer;

        import com.google.common.reflect.TypeToken;
        import com.pixelatedslice.easyconfig.api.serialization.Serializer;
        import com.pixelatedslice.easyconfig.api.serialization.SerializerRegistryOptions;
        import com.pixelatedslice.easyconfig.impl.serializer.SerializerRegistryImpl;
        import org.jspecify.annotations.NullMarked;
        import org.junit.jupiter.api.Assertions;
        import org.junit.jupiter.api.Test;
        import org.mockito.Mockito;

        @NullMarked public class SerializerRegistryTests{

        @SuppressWarnings("unchecked")
        private<T>Serializer<T>mockSerializer(Class<T>clazz){
        final var mockedSerializer=(Serializer<T>)Mockito.mock(Serializer.class);
        Mockito.when(mockedSerializer.type()).thenReturn(TypeToken.of(clazz));
        return mockedSerializer;
        }

        @Test public void SerializerRegistry_can_register_serializer(){
        // Arrange
        final var mockedSerializer=this.mockSerializer(String.class);
        final var serializerRegistry=new SerializerRegistryImpl(null);

        // Act
        serializerRegistry.register(mockedSerializer);

        // Assert
        final var result=serializerRegistry.serializerFor(String.class).orElseThrow();
        Assertions.assertEquals(mockedSerializer,result);
        }

        @Test public void SerializerRegistry_can_register_serializer_duplication_maintain_first(){
        // Arrange
        final var mockedSerializer=this.mockSerializer(String.class);
        final var copy=this.mockSerializer(String.class);
        final var serializerRegistry=new SerializerRegistryImpl(null);

        serializerRegistry.register(mockedSerializer);

        // Act
        serializerRegistry.register(option->option.duplicateRegisterStyle(SerializerRegistryOptions.DuplicateRegisterStyle.KEEP_ORIGINAL),
        copy);

        // Assert
        final var result=serializerRegistry.serializerFor(String.class).orElseThrow();
        Assertions.assertEquals(mockedSerializer,result);
        }

        @Test public void SerializerRegistry_can_register_serializer_duplication_replace(){
        // Arrange
        final var mockedSerializer=this.mockSerializer(String.class);
        final var copy=this.mockSerializer(String.class);
        final var serializerRegistry=new SerializerRegistryImpl(null);

        serializerRegistry.register(mockedSerializer);

        // Act
        serializerRegistry.register(option->option.duplicateRegisterStyle(SerializerRegistryOptions.DuplicateRegisterStyle.REPLACE),
        copy);

        // Assert
        final var result=serializerRegistry.serializerFor(String.class).orElseThrow();
        Assertions.assertEquals(copy,result);
        }

        @Test public void SerializerRegistry_can_register_serializer_duplication_throws(){
        // Arrange
        final var mockedSerializer=this.mockSerializer(String.class);
        final var copy=this.mockSerializer(String.class);
        final var serializerRegistry=new SerializerRegistryImpl(null);

        serializerRegistry.register(mockedSerializer);

        // Act
        Assertions.assertThrows(IllegalArgumentException.class,()->{
        serializerRegistry.register(option->option.duplicateRegisterStyle(SerializerRegistryOptions.DuplicateRegisterStyle.THROW),
        copy);
        });

        // Assert
        final var result=serializerRegistry.serializerFor(String.class).orElseThrow();
        Assertions.assertEquals(mockedSerializer,result);
        }

        @Test public void SerializerRegistry_stream_from_child(){
        // Arrange
        final var mockedSerializer=this.mockSerializer(String.class);
        final var serializerRegistry=new SerializerRegistryImpl(null);

        serializerRegistry.register(mockedSerializer);

        // Act
        final var result=serializerRegistry.stream().findAny().orElseThrow();

        // Assert
        Assertions.assertEquals(mockedSerializer,result);
        }

        @Test public void SerializerRegistry_createChild_get_from_parent(){
        // Arrange
        final var mockedSerializer=this.mockSerializer(String.class);
        final var serializerRegistry=new SerializerRegistryImpl(null);
        final var child=new SerializerRegistryImpl(serializerRegistry);

        serializerRegistry.register(mockedSerializer);

        // Act
        final var result=child.serializerFor(String.class).orElseThrow();

        // Assert
        Assertions.assertEquals(mockedSerializer,result);
        }

        @Test public void SerializerRegistry_createChild_stream_from_child(){
        // Arrange
        final var mockedSerializer=this.mockSerializer(String.class);
        final var serializerRegistry=new SerializerRegistryImpl(null);
        final var child=new SerializerRegistryImpl(serializerRegistry);

        serializerRegistry.register(mockedSerializer);

        // Act
        final var result=child.stream().findAny().orElseThrow();

        // Assert
        Assertions.assertEquals(mockedSerializer,result);
        }
        }
        package com.pixelatedslice.easyconfig.impl.test.testUtils;

        import org.jspecify.annotations.NullMarked;

        import org.junit.jupiter.api.Assertions;

        import java.util.Collection;
        import java.util.LinkedList;

        @NullMarked public class CollectionAssert{

        public static<Value>void isEqualTo(Collection<Value>expected,Collection<Value>compare,boolean matchOrder){
        if(matchOrder){
        isEqualToOrdered(expected,compare);
        return;
        }
        isEqualToUnordered(expected,compare);
        }

        private static<Value>void isEqualToOrdered(Collection<Value>expected,Collection<Value>compare){
        if(expected.size()!=compare.size()){
        Assertions.assertEquals(expected.size(),compare.size(),"Size do not match");
        return;
        }
        final var expectedIterator=expected.iterator();
        final var compareIterator=compare.iterator();
        int index=0;
        while(expectedIterator.hasNext()){
        final var expectedValue=expectedIterator.next();
        final var compareValue=compareIterator.next();
        Assertions.assertEquals(expectedValue,compareValue,"values did not match at "+index);
        index++;
        }
        }

        private static<Value>void isEqualToUnordered(Collection<Value>expected,Collection<Value>compare){
        if(expected.size()!=compare.size()){
        Assertions.assertEquals(expected.size(),compare.size(),"Size do not match");
        return;
        }
        final var mutableCompare=new LinkedList<>(compare);
        for(var entry:expected){
        if(mutableCompare.contains(entry)){
        mutableCompare.remove(entry);
        }else{
        Assertions.fail("Could not find entry "+entry);
        return;
        }
        }
        }
        }
        package com.pixelatedslice.easyconfig.impl.test.testUtils;

        import org.jspecify.annotations.NullMarked;

        import org.mockito.Mockito;

        @NullMarked public class MockitoHelper{

        public static<T>T whenReturn(T methodCall,T value){
        Mockito.when(methodCall).thenReturn(value);
        return value;
        }
        }
        package com.pixelatedslice.easyconfig.impl.test.utils;

        import com.pixelatedslice.easyconfig.impl.utils.DistinctByGatherer;
        import org.jspecify.annotations.NullMarked;
        import org.junit.jupiter.api.Assertions;
        import org.junit.jupiter.api.Test;

        import java.util.List;

        @NullMarked public class DistinctByGathererTests{

        @Test public void DistinctByGatherer_filter_and_remove_entries(){
        //ARRANGE
        final var original=List.of("A","B","CD","EF","GHI","JKL");
        final var expected=List.of("A","CD","GHI");

        //ACT
        final var result=original.stream().gather(new DistinctByGatherer<>(String::length)).toList();

        //ASSERT
        Assertions.assertEquals(expected,result);

        }

        @Test public void DistinctByGatherer_split_filter_and_remove_entries(){
        //ARRANGE
        final var original=List.of("A","B","CD","EF","GHI","JKL");
        final var expected=List.of("A","CD","GHI");

        //ACT
        final var result=original.stream().parallel().gather(new DistinctByGatherer<>(String::length)).toList();

        //ASSERT
        Assertions.assertEquals(expected,result);

        }
        }
