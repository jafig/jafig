module jafig {
    requires com.google.common;
    requires org.jspecify;

    exports com.badminimum.jafig.api.config;
    exports com.badminimum.jafig.api.config.edit;
    exports com.badminimum.jafig.api.config.exception;
    exports com.badminimum.jafig.api.config.node;
    exports com.badminimum.jafig.api.config.node.value;
    exports com.badminimum.jafig.api.config.node.path;
    exports com.badminimum.jafig.api.config.validator;
    exports com.badminimum.jafig.api.config.env;
}