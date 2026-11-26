package com.badminimum.jafig.impl.config.node.path;

import com.badminimum.jafig.api.config.node.path.StructuralNodePath;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class StructuralNodePathImpl extends AbstractNodePath implements StructuralNodePath {
    public StructuralNodePathImpl(ImmutableList<String> keys) {
        super(keys);
    }
}
