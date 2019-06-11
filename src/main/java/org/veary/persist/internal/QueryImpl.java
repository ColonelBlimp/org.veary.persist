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
import org.veary.persist.SqlBuilder;
import org.veary.persist.exceptions.NoResultException;
import org.veary.persist.exceptions.NonUniqueResultException;
import org.veary.persist.exceptions.PersistenceException;

/**
 * Handles SQL statement which return read and return 0 or more results in a {@link ResultSet}.
 *
 * @author Marc L. Veary
 * @since 1.0
 */
public final class QueryImpl implements Query {

    private static final String SELECT_STR = "SELECT";
    private static final String ENTITY_FACTORY_METHOD = "newInstance";

    private final DataSource ds;
    private final SqlBuilder builder;
    private final Class<?> entityInterface;
    private final Map<String, Object> parameters;

    private List<Map<String, Object>> internalResult;

    /**
     * Constructor.
     *
     * @param ds {@link DataSource}
     * @param builder {@link SqlBuilder}
     * @param entityInterface the interface of the Entity class
     */
    public QueryImpl(DataSource ds, SqlBuilder builder, Class<?> entityInterface) {
        this.ds = Objects.requireNonNull(ds,
            Messages.getString("QueryImpl.error_msg_ds_null")); //$NON-NLS-1$
        this.builder = Objects.requireNonNull(builder,
            Messages.getString("QueryImpl.error_msg_builder_null")); //$NON-NLS-1$
        this.entityInterface = Objects.requireNonNull(entityInterface,
            Messages.getString("QueryImpl.error_msg_iface_null")); //$NON-NLS-1$
        this.parameters = new HashMap<>();
    }

    @Override
    public Query setParameter(int index, Object value) {
        if (index < 1) {
            throw new IllegalArgumentException(
                Messages.getString("QueryImpl.error_msg_invalid_index")); //$NON-NLS-1$
        }
        this.parameters.put(String.valueOf(index), Objects.requireNonNull(value,
            Messages.getString("QueryImpl.error_msg_null_value"))); //$NON-NLS-1$
        return this;
    }

    @Override
    public Query execute() {
        if (!this.builder.toString().startsWith(SELECT_STR)) {
            throw new IllegalStateException(
                Messages.getString("QueryImpl.error_msg_incorrect_query_type")); //$NON-NLS-1$
        }

        try (Connection conn = this.ds.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(this.builder.toString())) {

                for (final Map.Entry<String, Object> param : this.parameters.entrySet()) {
                    stmt.setObject(Integer.valueOf(param.getKey()), param.getValue());
                }

                try (ResultSet rset = stmt.executeQuery()) {
                    this.internalResult = processResultSet(rset);
                }

            }
        } catch (final SQLException e) {
            throw new PersistenceException(e);
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

        return getNewInstance(getStaticFactoryMethod(), this.internalResult.get(0));
    }

    @Override
    public List<Object> getResultList() {
        if (this.internalResult == null) {
            throw new PersistenceException(
                Messages.getString("QueryImpl.error_msg_method_sequence")); //$NON-NLS-1$
        }

        return Collections.emptyList();
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
                row.put(md.getColumnName(i).toUpperCase(), rset.getObject(i));
            }
            list.add(row);
        }

        return list;
    }

    private Method getStaticFactoryMethod() {
        try {
            return this.entityInterface.getDeclaredMethod(ENTITY_FACTORY_METHOD, Map.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new PersistenceException(e);
        }
    }

    private Object getNewInstance(Method staticFactory, Map<String, Object> result) {
        try {
            return staticFactory.invoke(this.entityInterface, result);
        } catch (IllegalAccessException | IllegalArgumentException
            | InvocationTargetException e) {
            throw new PersistenceException(e);
        }
    }
}
