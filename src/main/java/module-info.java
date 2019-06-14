module org.veary.persist {
    exports org.veary.persist;
    exports org.veary.persist.exceptions;
    exports org.veary.persist.internal to com.google.guice;

    requires com.google.guice;
    requires com.google.guice.extensions.jndi;
    requires java.naming;
    requires transitive java.sql;
    requires javax.inject;
    requires org.apache.logging.log4j;

    provides com.google.inject.AbstractModule with org.veary.persist.internal.GuicePersistModule;
}
