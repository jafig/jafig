package com.badminimum.jafig.impl.config.node.value;

import com.badminimum.jafig.api.config.env.EnvBinding;
import com.badminimum.jafig.api.config.exception.TypeException;
import com.badminimum.jafig.api.config.node.path.TypedNodePath;
import com.badminimum.jafig.api.config.node.value.ValueNode;
import com.badminimum.jafig.api.config.serialization.JafigSerializer;
import com.badminimum.jafig.api.config.validator.Validator;
import com.badminimum.jafig.impl.config.node.AbstractNode;
import com.badminimum.jafig.impl.utils.typetoken.TypeTokenUtils;
import com.google.common.reflect.TypeToken;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("ConstructorWithTooManyParameters")
@NullMarked
public class ValueNodeImpl<T> extends AbstractNode implements ValueNode<T> {
    private final TypedNodePath<T> typedNodePath;
    private final @Nullable T value;
    private final @Nullable T defaultValue;
    private final @Nullable EnvBinding<@Nullable T> envBinding;
    private final Validator<@Nullable T> validator;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<TypeToken<? extends JafigSerializer<T>>> serializerOpt;

    public ValueNodeImpl(TypedNodePath<T> typedNodePath, EnvBinding<@Nullable T> envBinding, @Nullable T defaultValue,
            Validator<@Nullable T> validator, @Nullable TypeToken<? extends JafigSerializer<T>> serializer) {
        super(Objects.requireNonNull(typedNodePath));
        this.typedNodePath = typedNodePath;
        this.envBinding = Objects.requireNonNull(envBinding);
        this.defaultValue = defaultValue;
        this.validator = Objects.requireNonNull(validator);
        this.serializerOpt = Optional.ofNullable(serializer);

        final var variable = envBinding.variable();
        final var adapter = envBinding.adapter();
        final var fromEnv = System.getenv(variable);
        if (fromEnv == null) {
            this.value = null;
            return;
        }
        if (adapter != null) {
            this.value = adapter.apply(fromEnv);
            return;
        }
        if (TypeTokenUtils.hasCorrectType(fromEnv, typedNodePath.typeToken())) {
            //noinspection unchecked
            this.value = (T) fromEnv;
        } else {
            throw TypeException.TYPES_DO_NOT_MATCH(typedNodePath.typeToken(), TypeToken.of(String.class));
        }
    }

    public ValueNodeImpl(TypedNodePath<T> typedNodePath, @Nullable T value, @Nullable T defaultValue,
            Validator<@Nullable T> validator, @Nullable TypeToken<? extends JafigSerializer<T>> serializer) {
        super(typedNodePath);
        this.typedNodePath = typedNodePath;
        this.value = value;
        this.defaultValue = defaultValue;
        this.validator = validator;
        this.serializerOpt = Optional.ofNullable(serializer);
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
    public Validator<@Nullable T> validator() {
        return this.validator;
    }

    @Override
    public Optional<TypeToken<? extends JafigSerializer<T>>> serializer() {
        return this.serializerOpt;
    }

    @Override
    public Optional<EnvBinding<@Nullable T>> env() {
        return Optional.ofNullable(this.envBinding);
    }
}
