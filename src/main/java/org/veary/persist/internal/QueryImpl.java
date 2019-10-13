/*
 * MIT License
 *
 * Copyright (c) 2019 ColonelBlimp
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.veary.persist.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import org.veary.persist.Query;
import org.veary.persist.SqlStatement;
import org.veary.persist.exceptions.NoResultException;
import org.veary.persist.exceptions.NonUniqueResultException;
import org.veary.persist.exceptions.PersistenceException;

/**
 * Concrete implementation of {@link Query}.
 *
 * @author Marc L. Veary
 * @since 1.0
 */
public final class QueryImpl implements Query {

    private static final String SELECT_STR = "SELECT";
    private static final String ENTITY_FACTORY_METHOD = "newInstance";

    private final DataSource ds;
    private final SqlStatement statement;
    private Class<?> entityInterface;

    private List<Map<String, Object>> internalResult;

    /**
     * Constructor.
     *
     * @param ds {@link DataSource}
     * @param statement {@link SqlStatement}
     * @param entityInterface the interface of a class which is to be created (by Reflection)
     *     and returned as the result(s).
     *
     *     <p> This interface must define a <b>static method</b> with the signature:
     *
     *     <pre>
     *     newInstance(Map&lt;String, Object&gt;)
     *     </pre>
     *
     *     <p> Which should validate the input {@code Map} and populate the instance's member
     *     fields.
     */
    public QueryImpl(DataSource ds, SqlStatement statement, Class<?> entityInterface) {
        this.ds = Objects.requireNonNull(ds,
            Messages.getString("QueryImpl.error_msg_ds_null")); //$NON-NLS-1$
        this.statement = Objects.requireNonNull(statement,
            Messages.getString("QueryImpl.error_msg_statement_null")); //$NON-NLS-1$
        this.entityInterface = Objects.requireNonNull(entityInterface,
            Messages.getString("QueryImpl.error_msg_iface_null")); //$NON-NLS-1$
    }

    /**
     * Constructor.
     *
     * @param ds {@link DataSource}
     * @param statement {@link SqlStatement}
     */
    public QueryImpl(DataSource ds, SqlStatement statement) {
        this.ds = Objects.requireNonNull(ds,
            Messages.getString("QueryImpl.error_msg_ds_null")); //$NON-NLS-1$
        this.statement = Objects.requireNonNull(statement,
            Messages.getString("QueryImpl.error_msg_statement_null")); //$NON-NLS-1$
    }

    @Override
    public Query execute() {
        if (!this.statement.toString().toUpperCase().startsWith(SELECT_STR)) {
            throw new IllegalStateException(
                Messages.getString("QueryImpl.error_msg_incorrect_query_type")); //$NON-NLS-1$
        }

        try (Connection conn = this.ds.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(this.statement.toString())) {

                for (final Map.Entry<Integer, Object> param : this.statement.getParameters()
                    .entrySet()) {
                    stmt.setObject(param.getKey().intValue(), param.getValue());
                }

                try (ResultSet rset = stmt.executeQuery()) {
                    this.internalResult = processResultSet(rset);
                }

            }
        } catch (final SQLException e) {
            if (e.getCause() == null) {
                throw new PersistenceException(e);
            }
            throw new PersistenceException(e.getCause());
        }

        return this;
    }

    @Override
    public Object getSingleResult() {
        if (this.internalResult == null) {
            throw new PersistenceException(
                Messages.getString("QueryImpl.error_msg_method_sequence")); //$NON-NLS-1$
        }

        if (this.internalResult.size() > 1) {
            throw new NonUniqueResultException(
                Messages.getString("QueryImpl.error_msg_too_many_results")); //$NON-NLS-1$
        }

        if (this.entityInterface == null) {
            Map<String, Object> result = this.internalResult.get(0);
            return result.values().toArray()[0];
        }

        return getNewInstance(getStaticFactoryMethod(), this.internalResult.get(0));
    }

    @Override
    public List<Object> getResultList() {
        if (this.internalResult == null) {
            throw new PersistenceException(
                Messages.getString("QueryImpl.error_msg_method_sequence")); //$NON-NLS-1$
        }

        if (this.entityInterface == null) {
            throw new UnsupportedOperationException();
        }

        final List<Object> list = new ArrayList<>(this.internalResult.size());
        for (Map<String, Object> dataMap : this.internalResult) {
            list.add(getNewInstance(getStaticFactoryMethod(), dataMap));
        }

        return Collections.unmodifiableList(list);
    }

    /**
     * Process the given {@link ResultSet} into an {@code List<Map<String, Object>>}.
     *
     * @param rset {@code ResultSet}
     * @return a {@code List<Map<String, Object>>}. Cannot return {@code null}.
     * @throws SQLException if a database access error occurs
     * @throws NoResultException if this {@code Query} did not return any results
     */
    private List<Map<String, Object>> processResultSet(ResultSet rset) throws SQLException {
        if (!rset.isBeforeFirst()) {
            throw new NoResultException(
                Messages.getString("QueryImpl.error_msg_no_results")); //$NON-NLS-1$
        }

        final ResultSetMetaData md = rset.getMetaData();
        final List<Map<String, Object>> list = new ArrayList<>();

        final int columns = md.getColumnCount();
        while (rset.next()) {
            final Map<String, Object> row = new HashMap<>();
            for (int i = 1; i <= columns; i++) {
                row.put(md.getColumnLabel(i).toUpperCase(), rset.getObject(i));
            }
            list.add(row);
        }

        return list;
    }

    /**
     * Ensures that the Constructor declared {@code Class<?> entityInterface} parameter has a
     * declared <b>static method</b> named <b>newInstance</b> and takes a single parameter of
     * type {@code Map}.
     *
     * @return {@link Method}
     */
    private Method getStaticFactoryMethod() {
        try {
            return this.entityInterface.getDeclaredMethod(ENTITY_FACTORY_METHOD, Map.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new PersistenceException(
                String.format("Error accessing %s.newInstance(): %s - cause: %s",
                    this.entityInterface.getName(),
                    e, e.getCause()));
        }
    }

    /**
     * Invokes the method returned by {@link #getStaticFactoryMethod()}.
     *
     * @param staticFactory {@link Method}
     * @param result {@code Map} result from the query
     * @return {@link Object}
     */
    private Object getNewInstance(Method staticFactory, Map<String, Object> result) {
        try {
            return staticFactory.invoke(this.entityInterface, result);
        } catch (IllegalAccessException | IllegalArgumentException
            | InvocationTargetException e) {
            throw new PersistenceException(
                String.format("Error invoking %s.newInstance(): %s - cause: %s",
                    this.entityInterface.getName(),
                    e, e.getCause()));
        }
    }
}
