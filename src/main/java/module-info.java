module org.veary.persist {
    exports org.veary.persist;
    exports org.veary.persist.exceptions;

    requires com.google.guice;
    requires com.google.guice.extensions.jndi;
    requires javax.inject;
    requires org.apache.logging.log4j;

    requires transitive java.sql;
    requires java.naming;
}
