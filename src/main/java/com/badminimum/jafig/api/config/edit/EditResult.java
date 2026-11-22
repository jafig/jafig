package com.badminimum.jafig.api.config.edit;

import com.badminimum.jafig.api.config.Config;
import com.google.common.collect.ImmutableList;

public sealed interface EditResult permits EditResult.Conflict, EditResult.Invalid, EditResult.Success {
    record Success(Config snapshot) implements EditResult {
    }

    record Invalid(ImmutableList<EditError> errors) implements EditResult {
    }

    record Conflict(Config current) implements EditResult {
    }
}

