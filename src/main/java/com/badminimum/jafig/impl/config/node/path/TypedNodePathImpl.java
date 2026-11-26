package com.badminimum.jafig.impl.config.node.path;

import com.badminimum.jafig.api.config.node.path.TypedNodePath;
import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class TypedNodePathImpl<T> extends AbstractNodePath implements TypedNodePath<T> {
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
