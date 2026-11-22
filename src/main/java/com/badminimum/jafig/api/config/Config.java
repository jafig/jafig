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
