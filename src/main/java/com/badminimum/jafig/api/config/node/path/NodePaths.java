package com.badminimum.jafig.api.config.node.path;

import com.badminimum.jafig.api.config.exception.NodePathException;
import com.badminimum.jafig.impl.config.node.path.StructuralNodePathImpl;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NullMarked;

import java.util.Arrays;
import java.util.Collection;

@NullMarked
public interface NodePaths {
    StructuralNodePath ROOT = new StructuralNodePathImpl(ImmutableList.of());

    static <P extends NodePath> P from(Creator<P> creator, StructuralNodePath parent, Collection<String> keys)
            throws NodePathException {
        return creator.create(parent, normalize(keys));
    }

    static <P extends NodePath> P from(Creator<P> creator, Collection<String> keys) throws NodePathException {
        return from(creator, ROOT, normalize(keys));
    }

    static <P extends NodePath> P from(Creator<P> creator, StructuralNodePath parent, String... keys)
            throws NodePathException {
        return from(creator, parent, normalize(Arrays.asList(keys)));
    }

    static <P extends NodePath> P from(Creator<P> creator, String... keys) throws NodePathException {
        return from(creator, normalize(Arrays.asList(keys)));
    }

    static <P extends NodePath> P fromDotted(Creator<P> creator, StructuralNodePath parent, String dotted)
            throws NodePathException {
        return from(creator, parent, normalize(Arrays.asList(dotted.split("\\.", -1))));
    }

    static <P extends NodePath> P fromDotted(Creator<P> creator, String dotted) throws NodePathException {
        return from(creator, normalize(Arrays.asList(dotted.split("\\.", -1))));
    }

    private static ImmutableList<String> normalize(Collection<String> keys) throws NodePathException {
        //noinspection ConstantValue
        if (keys == null) {
            throw NodePathException.KEY_COLLECTION_IS_NULL();
        }

        var invalidKeys = 0;
        for (var key : keys) {
            //noinspection ConstantValue
            if ((key == null) || key.isEmpty()) {
                invalidKeys++;
            }
        }

        if (invalidKeys > 0) {
            throw NodePathException.KEY_IS_INVALID(invalidKeys);
        }

        return (keys instanceof ImmutableList<String> immutable) ? immutable : ImmutableList.copyOf(keys);
    }

    @FunctionalInterface
    interface Creator<P extends NodePath> {
        P create(StructuralNodePath parent, ImmutableList<String> keys);
    }
}