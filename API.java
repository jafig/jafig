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
package com.badminimum.jafig.api.config.node.value;

import com.badminimum.jafig.api.config.node.Node;
import com.badminimum.jafig.api.config.node.path.TypedNodePath;
import com.badminimum.jafig.api.config.validator.Validator;

import java.util.Optional;

public non-sealed interface ValueNode<T> extends Node {
    @Override
    TypedNodePath<T> path();

    Optional<T> value();

    Optional<T> defaultValue();

    default Optional<T> valueOrDefault() {
        return this.value().or(this::defaultValue);
    }

    Validator<T> validator();

    Optional<EnvBinding<T>> env();
}package com.badminimum.jafig.api.config.node.value;

import com.badminimum.jafig.api.config.env.EnvAdapter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record EnvBinding<T>(String variable, @Nullable EnvAdapter<T> adapter) {
}
package com.badminimum.jafig.api.config.node.value;

import com.badminimum.jafig.api.config.env.EnvAdapter;
import com.badminimum.jafig.api.config.validator.Validator;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

@NullMarked
public interface ValueNodeBuilder {
    interface Shared<T, Current extends ValueNodeBuilder> {
        Current validator(Validator<@Nullable T> validator);

        Current defaultValue(@Nullable T defaultValue);

        Current comments(String... comments);

        Current comments(Collection<String> comments);
    }

    interface Default<T> extends ValueNodeBuilder, Shared<T, Default<T>>, BuildStep<T> {
        EnvLocked<T> env(String variable);

        ValueLocked<T> value(@Nullable T value);
    }

    interface ValueLocked<T> extends ValueNodeBuilder, Shared<T, ValueLocked<T>>, BuildStep<T> {
    }

    interface EnvLocked<T> extends ValueNodeBuilder, Shared<T, EnvLocked<T>>, BuildStep<T> {
        EnvLocked<T> adapter(EnvAdapter<T> adapter);
    }

    interface BuildStep<T> {
        ValueNode<T> build();
    }
}package com.badminimum.jafig.api.config.node.path;

import com.badminimum.jafig.impl.config.node.path.TypedNodePathImpl;
import com.google.common.reflect.TypeToken;
import org.jspecify.annotations.NullMarked;

@NullMarked
public non-sealed interface TypedNodePath<T> extends NodePath {
    static <T> TypedNodePath<T> of(TypeToken<T> type, NodePath path) {
        return new TypedNodePathImpl<>(type, path.keys());
    }

    TypeToken<T> typeToken();
}
package com.badminimum.jafig.api.config.node.path;

import com.badminimum.jafig.api.config.exception.NodePathException;
import com.badminimum.jafig.impl.config.node.path.NodePathImpl;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

@NullMarked
public sealed interface NodePath permits TypedNodePath, NodePathImpl {
    ImmutableList<String> keys();

    default Optional<NodePath> parent() {
        if (this.isRoot()) {
            return Optional.empty();
        }

        try {
            return Optional.of(NodePaths.of(this.keys().subList(0, this.keys().size() - 1)));
        } catch (NodePathException e) {
            return Optional.empty();
        }
    }

    default NodePath child(String... more) throws NodePathException {
        return NodePaths.of(more);
    }

    default NodePath child(NodePath other) {
        return new NodePathImpl(ImmutableList.<String>builder().addAll(this.keys()).addAll(other.keys()).build());
    }

    default boolean isRoot() {
        return this.keys().isEmpty();
    }

    default int depth() {
        return this.keys().size();
    }

    default Optional<String> name() {
        return this.isRoot() ? Optional.of("") : Optional.of(this.keys().getLast());
    }
}
package com.badminimum.jafig.api.config.node.path;

import com.badminimum.jafig.api.config.exception.NodePathException;
import com.badminimum.jafig.impl.config.node.path.NodePathImpl;
import com.badminimum.jafig.impl.config.node.path.TypedNodePathImpl;
import com.badminimum.jafig.impl.utils.typetoken.TypeTokenUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;

@NullMarked
public interface NodePaths {
    @SuppressWarnings("ConstantValue")
    static void isValidKeyCollection(Collection<String> keys) {
        if (keys == null) {
            throw NodePathException.KEY_COLLECTION_IS_NULL;
        }

        var invalidKeys = 0;
        for (String key : keys) {
            if ((key == null) || key.isEmpty()) {
                invalidKeys++;
            }
        }

        if (invalidKeys != 0) {
            throw NodePathException.KEY_IS_INVALID(invalidKeys);
        }
    }

    static void isValidDotted(String dotted) {
        if (dotted.isEmpty()) {
            throw NodePathException.KEY_IS_INVALID(1);
        }
    }

    static NodePath of(ImmutableList<String> keys) throws NodePathException {
        isValidKeyCollection(keys);
        return new NodePathImpl(keys);
    }

    static NodePath of(Collection<String> keys) throws NodePathException {
        isValidKeyCollection(keys);
        return NodePaths.of(ImmutableList.copyOf(keys));
    }

    static NodePath of(String... keys) {
        return NodePaths.of(ImmutableList.copyOf(keys));
    }

    static NodePath parse(String dotted) throws NodePathException {
        isValidDotted(dotted);
        return NodePaths.of(ImmutableList.copyOf(dotted.split("\\.", -1)));
    }

    interface Typed {
        static <T> TypedNodePath<T> of(TypeToken<T> typeToken, ImmutableList<String> keys) throws NodePathException {
            isValidKeyCollection(keys);
            return new TypedNodePathImpl<T>(typeToken, keys);
        }

        static <T> TypedNodePath<T> of(Class<T> simpleType, ImmutableList<String> keys) throws NodePathException {
            isValidKeyCollection(keys);
            return new TypedNodePathImpl<T>(TypeTokenUtils.getSimpleOrThrow(simpleType), keys);
        }

        static <T> TypedNodePath<T> of(TypeToken<T> typeToken, Collection<String> keys) throws NodePathException {
            isValidKeyCollection(keys);
            return Typed.of(typeToken, ImmutableList.copyOf(keys));
        }

        static <T> TypedNodePath<T> of(Class<T> simpleType, Collection<String> keys) throws NodePathException {
            isValidKeyCollection(keys);
            return Typed.of(simpleType, ImmutableList.copyOf(keys));
        }

        static <T> TypedNodePath<T> of(TypeToken<T> typeToken, String... keys) {
            return Typed.of(typeToken, ImmutableList.copyOf(keys));
        }

        static <T> TypedNodePath<T> of(Class<T> simpleType, String... keys) {
            return Typed.of(simpleType, ImmutableList.copyOf(keys));
        }

        static <T> TypedNodePath<T> parse(TypeToken<T> typeToken, String dotted) throws NodePathException {
            isValidDotted(dotted);
            return Typed.of(typeToken, ImmutableList.copyOf(dotted.split("\\.", -1)));
        }

        static <T> TypedNodePath<T> parse(Class<T> simpleType, String dotted) throws NodePathException {
            isValidDotted(dotted);
            return Typed.of(simpleType, ImmutableList.copyOf(dotted.split("\\.", -1)));
        }
    }
}package com.badminimum.jafig.api.config.node.factory;

import com.badminimum.jafig.api.config.node.path.NodePath;
import com.badminimum.jafig.api.config.node.value.ValueNode;
import com.badminimum.jafig.api.config.node.value.ValueNodeBuilder;
import com.badminimum.jafig.impl.config.node.value.builder.ValueNodeBuilderImpl;
import com.badminimum.jafig.impl.utils.typetoken.TypeTokenUtils;
import com.google.common.reflect.TypeToken;

public interface Nodes {
    static <T> ValueNode<T> value(TypeToken<T> typeToken, NodePath nodePath, T defaultValue) {
        // TODO
        return null;
    }

    static <T> ValueNode<T> value(Class<T> simpleType, NodePath nodePath, T defaultValue) {
        return value(TypeTokenUtils.getSimpleOrThrow(simpleType), nodePath, defaultValue);
    }

    static <T> ValueNode<T> emptyValue(TypeToken<T> typeToken, NodePath nodePath) {
        return value(typeToken, nodePath, null);
    }

    static <T> ValueNode<T> emptyValue(Class<T> simpleType, NodePath nodePath) {
        return emptyValue(TypeTokenUtils.getSimpleOrThrow(simpleType), nodePath);
    }

    static <T> ValueNodeBuilder.Default<T> value(TypeToken<T> typeToken, NodePath nodePath) {
        return new ValueNodeBuilderImpl<>(typeToken, nodePath);
    }

    static <T> ValueNodeBuilder.Default<T> value(Class<T> simpleType, NodePath nodePath) {
        return value(TypeTokenUtils.getSimpleOrThrow(simpleType), nodePath);
    }
}
package com.badminimum.jafig.api.config.node.group;

import com.badminimum.jafig.api.config.node.Node;

public non-sealed interface GroupNode extends Node {
    Kind kind();

    enum Kind {
        OBJECT,
        LIST
    }
}
package com.badminimum.jafig.api.config;

import com.badminimum.jafig.api.config.edit.EditResult;
import com.badminimum.jafig.api.config.edit.EditTransaction;
import com.badminimum.jafig.api.config.node.Node;
import com.badminimum.jafig.api.config.node.path.NodePath;
import com.google.common.collect.ImmutableMap;

import java.util.function.Consumer;

public interface Config {
    ImmutableMap<NodePath, Node> nodes();

    Config edit(Consumer<EditTransaction> transactionConsumer);

    EditResult tryEdit(Consumer<EditTransaction> transaction);
}
package com.badminimum.jafig.api.config.edit;

import com.badminimum.jafig.api.config.Config;
import com.google.common.collect.ImmutableList;

public sealed interface EditResult permits EditResult.Conflict, EditResult.Invalid, EditResult.Success {
    record Success(Config snapshot) implements EditResult {
    }

    record Invalid(ImmutableList<EditError> errors) implements EditResult {
    }

    record Conflict(Config current) implements EditResult {
    }
}

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
package com.badminimum.jafig.api.config.edit;

import com.badminimum.jafig.api.config.node.path.NodePath;

public record EditError(NodePath path, String message) {
}
package com.badminimum.jafig.api.config.validator;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Predicate;

@FunctionalInterface
@NullMarked
public interface Validator<T extends @Nullable Object> {
    static <T> Validator<@Nullable T> of(Predicate<? super @Nullable T> predicate, String message) {
        return (@Nullable T value, ValidationContext ctx) -> {
            if (!predicate.test(value)) {
                ctx.error(message, (value == null) ? "null" : value);
            }
        };
    }

    default Validator<@Nullable T> and(Validator<? super @Nullable T> other) {
        return (@Nullable T value, ValidationContext ctx) -> {
            this.validate(value, ctx);
            other.validate(value, ctx);
        };
    }

    void validate(@Nullable T value, ValidationContext context);
}
package com.badminimum.jafig.api.config.validator;

import com.badminimum.jafig.api.config.node.path.NodePath;
import com.badminimum.jafig.api.config.node.path.TypedNodePath;
import org.jspecify.annotations.NullMarked;

import java.util.Optional;

@NullMarked
public interface ValidationContext {
    NodePath path();

    void error(String message, Object... args);

    void error(NodePath at, String message, Object... args);

    <V> Optional<V> get(TypedNodePath<V> other);
}package com.badminimum.jafig.api.config.validator;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@NullMarked
public final class Validators {
    private Validators() {
    }

    public static <T> Validator<T> empty() {
        return (T ignored, ValidationContext ignored2) -> {};
    }

    public static <T> Validator<@Nullable T> notNull() {
        return (@Nullable T item, ValidationContext ctx) -> {
            if (item != null) {
                return;
            }
            ctx.error("item is null");
        };
    }

    public static <T extends Comparable<? super T>> Validator<T> min(T minimum) {
        return (T item, ValidationContext ctx) -> {
            if (item.compareTo(minimum) >= 0) {
                return;
            }
            ctx.error("%s must be at least %s", item.toString(), minimum.toString());
        };
    }

    public static <T extends Comparable<? super T>> Validator<T> max(T maximum) {
        return (T item, ValidationContext ctx) -> {
            if (item.compareTo(maximum) <= 0) {
                return;
            }
            ctx.error("%s must be at most %s", item.toString(), maximum.toString());
        };
    }

    public static <T extends Comparable<? super T>> Validator<T> range(T min, T max) {
        return (T item, ValidationContext ctx) -> {
            if ((item.compareTo(min) >= 0) && (item.compareTo(max) <= 0)) {
                return;
            }

            ctx.error("%s must be between %s and %s (inclusive)", item.toString(), min.toString(), max.toString());
        };
    }

    public static Validator<String> notBlank() {
        return (String text, ValidationContext ctx) -> {
            if (!text.isBlank()) {
                return;
            }
            ctx.error("value must not be blank");
        };
    }

    public static Validator<String> matches(Pattern pattern) {
        return (String text, ValidationContext ctx) -> {
            final Matcher matcher = pattern.matcher(text);
            if (matcher.matches()) {
                return;
            }
            ctx.error("%s must match the pattern '%s'", text, pattern.pattern());
        };
    }

    @SafeVarargs
    public static <T> Validator<T> oneOf(T... items) {
        return oneOf(Arrays.asList(items));
    }

    public static <T> Validator<T> oneOf(Collection<? extends T> items) {
        return (T item, ValidationContext ctx) -> {
            if (items.contains(item)) {
                return;
            }
            ctx.error("%s must be one of %s", item.toString(), Arrays.toString(items.toArray()));
        };
    }

    @SafeVarargs
    public static <T> Validator<T> notIn(T... items) {
        return notIn(Arrays.asList(items));
    }

    public static <T> Validator<T> notIn(Collection<? extends T> items) {
        return (T item, ValidationContext ctx) -> {
            if (!items.contains(item)) {
                return;
            }
            ctx.error("%s must not be one of %s", item.toString(), Arrays.toString(items.toArray()));
        };
    }

    public static <T> Validator<T[]> notEmptyArray() {
        return (T[] items, ValidationContext ctx) -> {
            if (items.length != 0) {
                return;
            }
            ctx.error("array must not be empty");
        };
    }

    public static <C extends Collection<?>> Validator<C> notEmpty() {
        return (C item, ValidationContext ctx) -> {
            if (!item.isEmpty()) {
                return;
            }
            ctx.error("collection must not be empty");
        };
    }
}package com.badminimum.jafig.api.config.env;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Stream;

// All nulls in DefinedEnv are the culprits for DataFlowIssue warnings, but they are handled correctly in the code.
@SuppressWarnings({"unused", "DataFlowIssue"})
@NullMarked
public final class EnvKeys {

    public static final DefinedEnv<String> DEBUGGER_ID = new DefinedEnv<>("DEBUGGER_ID", (String t) -> t);
    public static final DefinedEnv<Boolean> DEBUGGING_ENABLED = new DefinedEnv<>("DEBUGGER_ENABLED",
            Boolean::parseBoolean);
    public static final DefinedEnv<@Nullable Integer> DISPLAY = new DefinedEnv<>("DISPLAY", (String input) -> {
        try {
            return Integer.parseInt(input.substring(1));
        } catch (NumberFormatException e) {
            return null;
        }
    });
    public static final DefinedEnv<@Nullable Path> EDITOR = DefinedEnv.path("EDITOR");
    public static final DefinedEnv<@Nullable Path> HOME = DefinedEnv.path("HOME");
    public static final DefinedEnv<@Nullable LangResult> LANG = new DefinedEnv<>("LANG", (String input) -> {
        String[] split = input.split("\\.", 2);
        return (split.length != 2) ? null : new LangResult(Locale.of(split[0]), split[1]);
    });
    public static final DefinedEnv<@Nullable Collection<Path>> PATHS = new DefinedEnv<>("PATH", (String input) -> {
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
    public static final DefinedEnv<@Nullable Path> SHELL = DefinedEnv.path("SHELL");
    public static final DefinedEnv<String> USER = new DefinedEnv<>("USER", (String t) -> t);

    private EnvKeys() {

    }
}

package com.badminimum.jafig.api.config.env;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

@NullMarked
public record DefinedEnv<T extends @Nullable Object>(String key, Function<String, @Nullable T> adapter) {
    static DefinedEnv<@Nullable Path> path(String key) {
        return new DefinedEnv<>(key, (String input) -> {
            try {
                return Path.of(input).toRealPath();
            } catch (IllegalArgumentException | IOException ex) {
                return null;
            }
        });
    }

}
package com.badminimum.jafig.api.config.env;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

@FunctionalInterface
@NullMarked
public interface EnvAdapter<T> extends Function<String, @Nullable T> {
}package com.badminimum.jafig.api.config.env;

import java.util.Locale;

public record LangResult(Locale language, String encoding) {

}
package com.badminimum.jafig.api.config.exception;

public final class NodePathException extends RuntimeException {
    public static final NodePathException KEY_COLLECTION_IS_NULL = new NodePathException(
            "The collection of keys for a node path cannot be null.");

    private NodePathException(String message, Object... args) {
        super(String.format(message, args));
    }

    public static NodePathException KEY_IS_INVALID(int count) {
        return new NodePathException("A key for a node path cannot be null or empty. (Contains %d invalid keys)",
                count);
    }
}
package com.badminimum.jafig.api.config.exception;

import com.google.common.reflect.TypeToken;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@SuppressWarnings("unused")
@NullMarked
public class TypeException extends RuntimeException {
    public TypeException(String message) {
        super(Objects.requireNonNull(message));
    }

    public TypeException(String message, Object... args) {
        super(String.format(Objects.requireNonNull(message), Objects.requireNonNull(args)));
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
}package com.badminimum.jafig.impl.config.node.path;

import com.badminimum.jafig.api.config.node.path.NodePath;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NullMarked;

@NullMarked
public non-sealed class NodePathImpl implements NodePath {
    private final ImmutableList<String> keys;

    public NodePathImpl(ImmutableList<String> keys) {
        this.keys = keys;
    }

    @Override
    public ImmutableList<String> keys() {
        return this.keys;
    }

    @Override
    public String toString() {
        return this.isRoot() ? "" : String.join(".", this.keys);
    }
}
package com.badminimum.jafig.impl.config.node.path;

import com.badminimum.jafig.api.config.node.path.TypedNodePath;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class TypedNodePathImpl<T> extends NodePathImpl implements TypedNodePath<T> {
    private final TypeToken<T> type;

    public TypedNodePathImpl(TypeToken<T> typeToken, ImmutableList<String> keys) {
        super(keys);
        this.type = typeToken;
    }

    @Override
    public TypeToken<T> typeToken() {
        return this.type;
    }
}
package com.badminimum.jafig.impl.config.node.value;

import com.badminimum.jafig.api.config.node.path.TypedNodePath;
import com.badminimum.jafig.api.config.node.value.EnvBinding;
import com.badminimum.jafig.api.config.node.value.ValueNode;
import com.badminimum.jafig.api.config.validator.Validator;
import com.badminimum.jafig.impl.config.node.AbstractNode;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

@SuppressWarnings("ConstructorWithTooManyParameters")
@NullMarked
public class ValueNodeImpl<T> extends AbstractNode implements ValueNode<T> {
    private final TypedNodePath<T> typedNodePath;
    private final @Nullable T value;
    private final @Nullable T defaultValue;
    private final Validator<@Nullable T> validator;
    private final @Nullable EnvBinding<T> envBinding;

    public ValueNodeImpl(TypedNodePath<T> typedNodePath, Collection<String> comments,
            @Nullable EnvBinding<T> envBinding, @Nullable T defaultValue, Validator<@Nullable T> validator) {
        super(typedNodePath, comments);
        this.typedNodePath = typedNodePath;
        this.envBinding = envBinding;
        this.defaultValue = defaultValue;
        this.validator = validator;
        this.value = null;
    }

    public ValueNodeImpl(TypedNodePath<T> typedNodePath, Collection<String> comments, @Nullable T value,
            @Nullable T defaultValue, Validator<@Nullable T> validator) {
        super(typedNodePath, comments);
        this.typedNodePath = typedNodePath;
        this.value = value;
        this.defaultValue = defaultValue;
        this.validator = validator;
        this.envBinding = null;
    }

    @Override
    public TypedNodePath<T> path() {
        return this.typedNodePath;
    }

    @Override
    public Optional<T> value() {
        return Optional.ofNullable(this.value);
    }

    @Override
    public Optional<T> defaultValue() {
        return Optional.ofNullable(this.defaultValue);
    }

    @Override
    public Validator<T> validator() {
        return this.validator;
    }

    @Override
    public Optional<EnvBinding<T>> env() {
        return Optional.ofNullable(this.envBinding);
    }
}
package com.badminimum.jafig.impl.config.node.value.builder;

import com.badminimum.jafig.api.config.node.path.TypedNodePath;
import com.badminimum.jafig.api.config.node.value.ValueNodeBuilder;
import com.badminimum.jafig.api.config.validator.Validator;
import com.badminimum.jafig.api.config.validator.Validators;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

@NullMarked
abstract class AbstractValueNodeBuilder<T, S extends ValueNodeBuilder> {
    protected final State<T> state;

    AbstractValueNodeBuilder(State<T> state) {
        this.state = state;
    }

    abstract S self();

    public S validator(Validator<@Nullable T> validator) {
        this.state.validator = validator;
        return this.self();
    }

    public S defaultValue(@Nullable T defaultValue) {
        this.state.defaultValue = defaultValue;
        return this.self();
    }

    public S comments(String... comments) {
        this.state.comments = List.of(comments);
        return this.self();
    }

    public S comments(Collection<String> comments) {
        this.state.comments = comments;
        return this.self();
    }

    static final class State<T> {
        final TypedNodePath<T> nodePath;
        Collection<String> comments = List.of();
        Validator<@Nullable T> validator = Validators.empty();
        @Nullable T defaultValue;

        State(TypedNodePath<T> nodePath) {
            this.nodePath = nodePath;
        }
    }
}

package com.badminimum.jafig.impl.config.node.value.builder;

import com.badminimum.jafig.api.config.env.EnvAdapter;
import com.badminimum.jafig.api.config.node.path.NodePath;
import com.badminimum.jafig.api.config.node.path.TypedNodePath;
import com.badminimum.jafig.api.config.node.value.EnvBinding;
import com.badminimum.jafig.api.config.node.value.ValueNode;
import com.badminimum.jafig.api.config.node.value.ValueNodeBuilder;
import com.badminimum.jafig.impl.config.node.value.ValueNodeImpl;
import com.google.common.reflect.TypeToken;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class ValueNodeBuilderImpl<T> extends AbstractValueNodeBuilder<T, ValueNodeBuilderImpl<T>>
        implements ValueNodeBuilder.Default<T> {

    public ValueNodeBuilderImpl(TypeToken<T> typeToken, NodePath nodePath) {
        super(new State<>(TypedNodePath.of(typeToken, nodePath)));
    }

    @Override
    ValueNodeBuilderImpl<T> self() {
        return this;
    }

    @Override
    public EnvLocked<T> env(String variable) {
        return new EnvLockedImpl<>(this.state, variable);
    }

    @Override
    public ValueLocked<T> value(@Nullable T value) {
        return new ValueLockedImpl<>(this.state, value);
    }

    @Override
    public ValueNode<T> build() {
        return new ValueLockedImpl<>(this.state, null).build();
    }

    static final class EnvLockedImpl<T> extends AbstractValueNodeBuilder<T, EnvLockedImpl<T>> implements EnvLocked<T> {
        private final String variable;
        private @Nullable EnvAdapter<T> adapter;

        EnvLockedImpl(State<T> state, String variable) {
            super(state);
            this.variable = variable;
        }

        @Override
        EnvLockedImpl<T> self() {
            return this;
        }

        @Override
        public EnvLockedImpl<T> adapter(EnvAdapter<T> adapter) {
            this.adapter = adapter;
            return this;
        }

        @Override
        public ValueNode<T> build() {
            return new ValueNodeImpl<>(this.state.nodePath,
                    this.state.comments,
                    new EnvBinding<>(this.variable, this.adapter),
                    this.state.defaultValue,
                    this.state.validator);
        }
    }

    static final class ValueLockedImpl<T> extends AbstractValueNodeBuilder<T, ValueLockedImpl<T>>
            implements ValueLocked<T> {
        private final @Nullable T value;

        ValueLockedImpl(State<T> state, @Nullable T value) {
            super(state);
            this.value = value;
        }

        @Override
        ValueLockedImpl<T> self() {
            return this;
        }

        @Override
        public ValueNode<T> build() {
            return new ValueNodeImpl<>(this.state.nodePath,
                    this.state.comments,
                    this.value,
                    this.state.defaultValue,
                    this.state.validator);
        }
    }
}
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
package com.badminimum.jafig.impl.utils.typetoken;

import com.google.common.reflect.TypeToken;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

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
}package com.badminimum.jafig.impl.utils.typetoken;

import com.badminimum.jafig.api.config.exception.TypeException;
import com.google.common.reflect.TypeToken;
import org.jspecify.annotations.NullMarked;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
}module jafig {
    requires com.google.common;
    requires org.jspecify;

    exports com.badminimum.jafig.api.config;
    exports com.badminimum.jafig.api.config.edit;
    exports com.badminimum.jafig.api.config.exception;
    exports com.badminimum.jafig.api.config.node;
    exports com.badminimum.jafig.api.config.node.value;
    exports com.badminimum.jafig.api.config.node.path;
    exports com.badminimum.jafig.api.config.validator;
    exports com.badminimum.jafig.api.config.env;
}