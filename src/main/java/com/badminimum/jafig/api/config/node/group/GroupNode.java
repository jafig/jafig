package com.badminimum.jafig.api.config.node.group;

import com.badminimum.jafig.api.config.node.Node;
import com.badminimum.jafig.api.config.node.path.ListNodePath;

public sealed interface GroupNode extends Node permits GroupNode.List {

    /// ```yaml
    /// names: # <- This is the ListNode
    /// - ...
    /// - ...
    /// ```
    non-sealed interface List extends GroupNode {
        @Override
        ListNodePath path();
    }

    /// ```yaml
    /// database: # <- This is the ObjectNode
    ///   host: ...
    ///   port: ...
    /// ```
    /// This interface doesn't exist since it is the default, if there is a GroupNode.List at a specific NodePath then
    /// it's a list, otherwise an object.
    /// <br />
    /// non-sealed interface ObjectNode extends GroupNode { }
}
