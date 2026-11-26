package com.badminimum.jafig.api.config.exception;

public final class NodePathException extends RuntimeException {
    private NodePathException(String message, Object... args) {
        super(String.format(message, args));
    }

    public static NodePathException KEY_COLLECTION_IS_NULL() {
        return new NodePathException("The collection of keys for a node path cannot be null.");
    }

    public static NodePathException KEY_IS_INVALID(int count) {
        return new NodePathException("A key for a node path cannot be null or empty. (Contains %d invalid keys)",
                count);
    }
}
