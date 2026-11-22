package com.badminimum.jafig.api.config.node.group;

import com.badminimum.jafig.api.config.node.Node;

public non-sealed interface GroupNode extends Node {
    Kind kind();

    enum Kind {
        OBJECT,
        LIST
    }
}
