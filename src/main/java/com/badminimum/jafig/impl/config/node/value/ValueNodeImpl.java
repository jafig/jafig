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
