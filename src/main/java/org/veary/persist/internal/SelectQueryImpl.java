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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import org.veary.persist.Entity;
import org.veary.persist.QueryBuilder;
import org.veary.persist.SelectQuery;
import org.veary.persist.exceptions.PersistenceException;

public final class SelectQueryImpl implements SelectQuery {

    private static final String SELECT_STR = "SELECT";

    private final DataSource ds;
    private final QueryBuilder queryBuilder;
    private final Class<?> entityInterface;
    private final Map<String, Object> parameters;

    public SelectQueryImpl(DataSource ds, QueryBuilder builder,
        Class<?> iface) {
        this.ds = Objects.requireNonNull(ds,
            Messages.getString("SelectQueryImpl.error_msg_ds_null"));
        this.queryBuilder = Objects.requireNonNull(builder,
            Messages.getString("SelectQueryImpl.error_msg_builder_null"));
        this.entityInterface = Objects.requireNonNull(iface,
            Messages.getString("SelectQueryImpl.error_msg_iface_null"));
        this.parameters = new HashMap<>();
    }

    @Override
    public SelectQuery setParameter(int index, Object value) {
        if (index < 1) {
            throw new IllegalArgumentException(
                Messages.getString("SelectQueryImpl.error_msg_invalid_index"));
        }
        this.parameters.put(String.valueOf(index),
            Objects.requireNonNull(value,
                Messages.getString("SelectQueryImpl.error_msg_invalid_value")));
        return this;
    }

    @Override
    public SelectQuery execute() {
        if (!this.queryBuilder.toString().startsWith(SELECT_STR)) {
            throw new IllegalStateException(
                Messages.getString("SelectQueryImpl.error_msg_incorrect_query_type"));
        }

        try (Connection conn = this.ds.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(this.queryBuilder.toString())) {

                for (final Map.Entry<String, Object> param : this.parameters.entrySet()) {
                    stmt.setObject(Integer.valueOf(param.getKey()), param.getValue());
                }

                try (ResultSet rset = stmt.executeQuery()) {
                    //                    this.internalResult = processResultSet(rset);
                }
            }
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
        return this;
    }

    @Override
    public Object getSingleResult() {
        return null;
    }

    @Override
    public List<? extends Entity> getResultList() {
        return null;
    }
}
