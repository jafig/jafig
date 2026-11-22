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
}