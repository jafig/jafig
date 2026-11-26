package com.badminimum.jafig.impl.config.node.value;

import com.badminimum.jafig.api.config.env.EnvAdapter;
import com.badminimum.jafig.api.config.env.EnvBinding;
import com.badminimum.jafig.api.config.node.builder.ValueNodeBuilder;
import com.badminimum.jafig.api.config.node.path.NodePath;
import com.badminimum.jafig.api.config.node.path.TypedNodePath;
import com.badminimum.jafig.api.config.node.value.ValueNode;
import com.badminimum.jafig.api.config.serialization.JafigSerializer;
import com.badminimum.jafig.api.config.validator.Validator;
import com.badminimum.jafig.api.config.validator.Validators;
import com.google.common.reflect.TypeToken;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class ValueNodeBuilderImpl<T>
        implements ValueNodeBuilder<T>, ValueNodeBuilder.EnvAdapterStep<T>, ValueNodeBuilder.BuildStep<T> {
    private final TypedNodePath<T> nodePath;
    private EnvOrValue<T> envOrValue = new EnvOrValue.Value<T>();
    private Validator<@Nullable T> validator = Validators.empty();
    private @Nullable TypeToken<? extends JafigSerializer<T>> serializer;
    private @Nullable T defaultValue;

    public ValueNodeBuilderImpl(NodePath nodePath, TypeToken<T> typeToken) {
        this.nodePath = TypedNodePath.of(nodePath, typeToken);
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
    public ValueNodeBuilderImpl<T> validator(Validator<@Nullable T> validator) {
        this.validator = validator;
        return this;
    }

    @Override
    public ValueNodeBuilder<T> serializer(TypeToken<? extends JafigSerializer<T>> serializer) {
        this.serializer = serializer;
        return this;
    }

    @Override
    public ValueNodeBuilderImpl<T> defaultValue(@Nullable T defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    @Override
    public ValueNode<T> build() {
        return switch (this.envOrValue) {
            case EnvOrValue.Env<T> env -> new ValueNodeImpl<>(this.nodePath,
                    env.toBinding(),
                    this.defaultValue,
                    this.validator,
                    this.serializer);
            case EnvOrValue.Value<T>() ->
                    new ValueNodeImpl<>(this.nodePath, (T) null, this.defaultValue, this.validator, this.serializer);
        };
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
