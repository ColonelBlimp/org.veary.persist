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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.veary.persist.Query;
import org.veary.persist.entity.Entity;
import org.veary.persist.exceptions.NoResultException;
import org.veary.persist.exceptions.PersistenceException;

public final class QueryImpl implements Query {

    private static final Logger LOG = LogManager.getLogger(QueryImpl.class);

    private final String nativeSql;
    private final DataSource ds;
    private final Map<String, Object> parameters;

    private Class<? extends Entity> entityInterface;
    private List<Map<String, Object>> internalResult;

    public QueryImpl(DataSource ds, String nativeSql) {
        this.ds = Objects.requireNonNull(ds, "DataSource parameter is null.");
        this.nativeSql = Objects.requireNonNull(nativeSql,
            "Native SQL statement parameter is null.");
        if ("".equals(this.nativeSql)) {
            throw new IllegalArgumentException("Native SQL statement must be non-empty.");
        }
        this.parameters = new HashMap<>();
    }

    public QueryImpl(DataSource ds, String nativeSql, Class<? extends Entity> entityInterface) {
        this(ds, nativeSql);
        this.entityInterface = Objects.requireNonNull(entityInterface,
            "Entity interface parameter is null.");
    }

    @Override
    public Object getSingleResult() {
        return getNewInstance(getStaticFactoryMethod(), this.internalResult.get(0));
    }

    @Override
    public List<? extends Entity> getResultList() {
        return Collections.emptyList();
    }

    @Override
    public Query setParameter(int index, Object value) {
        if (index < 1) {
            throw new IllegalArgumentException("Query parameter index starts a 1.");
        }
        this.parameters.put(String.valueOf(index), value);
        return this;
    }

    @Override
    public Query executeQuery() {
        if (!this.nativeSql.startsWith("SELECT")) {
            throw new IllegalArgumentException("");
        }

        try (Connection conn = this.ds.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(this.nativeSql,
                PreparedStatement.RETURN_GENERATED_KEYS)) {

                for (Map.Entry<String, Object> param : this.parameters.entrySet()) {
                    stmt.setObject(Integer.valueOf(param.getKey()), param.getValue());
                }

                try (ResultSet rset = stmt.executeQuery()) {
                    this.internalResult = processResultSet(rset);
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }

        return this;
    }

    @Override
    public Long executeUpdate() {
        if (this.nativeSql.startsWith("SELECT")) {
            throw new IllegalArgumentException("");
        }

        int result = 0;
        try (Connection conn = this.ds.getConnection()) {
            try (PreparedStatement stmt = conn
                .prepareStatement(this.nativeSql, PreparedStatement.RETURN_GENERATED_KEYS)) {

                for (Map.Entry<String, Object> param : this.parameters.entrySet()) {
                    stmt.setObject(Integer.valueOf(param.getKey()), param.getValue());
                }

                result = stmt.executeUpdate();
                if (stmt.getGeneratedKeys() != null) {
                    result = getGeneratedKey(stmt);
                }
            }
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }

        return Long.valueOf(result);
    }

    @Override
    public Query startTransaction() {
        return null;
    }

    private Method getStaticFactoryMethod() {
        LOG.trace("called");
        try {
            return this.entityInterface.getDeclaredMethod("newInstance", Map.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new PersistenceException(e);
        }
    }

    private Object getNewInstance(Method staticFactory, Map<String, Object> result) {
        LOG.trace("called");
        try {
            return staticFactory.invoke(this.entityInterface, result);
        } catch (IllegalAccessException | IllegalArgumentException
            | InvocationTargetException e) {
            throw new PersistenceException(e);
        }
    }

    private int getGeneratedKey(Statement stmt) {
        LOG.trace("called");
        try (ResultSet rset = stmt.getGeneratedKeys()) {
            if (rset != null && rset.next()) {
                return rset.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new PersistenceException(e);
        }
    }

    /**
     * Process the given {@link ResultSet} into an
     * {@code List<Map<String, Object>>}.
     *
     * @param rset the {@code ResultSet}
     * @return a {@code List<Map<String, Object>>}. Cannot be {@code null}, but can
     *         be empty.
     * @throws SQLException if there was a error
     */
    private List<Map<String, Object>> processResultSet(ResultSet rset) throws SQLException {
        LOG.trace("called");
        if (!rset.isBeforeFirst()) {
            throw new NoResultException();
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
}
