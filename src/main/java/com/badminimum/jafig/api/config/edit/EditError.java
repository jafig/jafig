package com.badminimum.jafig.api.config.edit;

import com.badminimum.jafig.api.config.node.path.NodePath;

public record EditError(NodePath path, String message) {
}
