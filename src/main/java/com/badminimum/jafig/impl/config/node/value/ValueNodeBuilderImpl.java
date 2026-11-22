package com.badminimum.jafig.impl.config.node.value;

import com.badminimum.jafig.api.config.env.EnvAdapter;
import com.badminimum.jafig.api.config.node.path.NodePath;
import com.badminimum.jafig.api.config.node.path.TypedNodePath;
import com.badminimum.jafig.api.config.node.value.EnvBinding;
import com.badminimum.jafig.api.config.node.value.ValueNode;
import com.badminimum.jafig.api.config.node.value.ValueNodeBuilder;
import com.badminimum.jafig.api.config.validator.Validator;
import com.google.common.reflect.TypeToken;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;

@NullMarked
public class ValueNodeBuilderImpl<T>
        implements ValueNodeBuilder<T>, ValueNodeBuilder.EnvAdapterStep<T>, ValueNodeBuilder.BuildStep<T> {
    private final TypeToken<T> typeToken;
    private final TypedNodePath<T> nodePath;
    private EnvOrValue<T> envOrValue = new EnvOrValue.Value<T>();
    private Validator<@Nullable T> validator;
    private @Nullable T defaultValue;
    private Collection<String> comments;

    public ValueNodeBuilderImpl(TypeToken<T> typeToken, NodePath nodePath) {
        this.typeToken = typeToken;
        this.nodePath = TypedNodePath.of(this.typeToken, nodePath);
    }

    @Override
    public EnvAdapterStep<T> env(String variable) {
        this.envOrValue = new EnvOrValue.Env<T>(variable, null);
        return this;
    }

    @Override
    public EnvAdapterStep<T> adapter(EnvAdapter<T> adapter) {
        this.envOrValue = new EnvOrValue.Env<>(((EnvOrValue.Env<T>) this.envOrValue).variable, adapter);
        return this;
    }

    @Override
    public ValueNode<T> build() {
        return switch (this.envOrValue) {
            case EnvOrValue.Env<T> env -> new ValueNodeImpl<T>(this.nodePath,
                    this.comments,
                    env.toBinding(),
                    this.defaultValue,
                    this.validator);
            case EnvOrValue.Value<T>() ->
                    new ValueNodeImpl<T>(this.nodePath, this.comments, (T) null, this.defaultValue, this.validator);
        };
    }

    @Override
    public ValueNodeBuilderImpl<T> validator(Validator<@Nullable T> validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public ValueNodeBuilderImpl<T> defaultValue(@Nullable T defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public ValueNodeBuilderImpl<T> comments(String... comments) {
        this.comments = List.of(comments);
        return this;
    }

    @Override
    public ValueNodeBuilderImpl<T> comments(Collection<String> comments) {
        this.comments = comments;
        return this;
    }

    private sealed interface EnvOrValue<T> permits EnvOrValue.Env, EnvOrValue.Value {
        record Env<T>(String variable, @Nullable EnvAdapter<T> adapter) implements EnvOrValue<T> {
            EnvBinding<T> toBinding() {
                return new EnvBinding<>(this.variable, this.adapter);
            }
        }

        record Value<T>() implements EnvOrValue<T> {
        }
    }
}
