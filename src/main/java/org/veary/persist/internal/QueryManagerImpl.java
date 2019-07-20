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

import java.util.Objects;

import javax.sql.DataSource;

import org.veary.persist.Query;
import org.veary.persist.QueryManager;
import org.veary.persist.SqlStatement;

/**
 * <h2>Purpose:</h2> handles read statements through JDBC.
 *
 * @author Marc L. Veary
 * @since 1.0
 */
public final class QueryManagerImpl implements QueryManager {

    private final DataSource ds;

    /**
     * Constructor.
     *
     * @param ds {@link DataSource}
     */
    public QueryManagerImpl(DataSource ds) {
        this.ds = Objects.requireNonNull(ds,
            Messages.getString("QueryManagerImpl.error_msg_ds_null"));
    }

    @Override
    public Query createQuery(SqlStatement statement, Class<?> entityInterface) {
        return new QueryImpl(this.ds, Objects.requireNonNull(statement,
            Messages.getString("QueryManagerImpl.error_msg_statement_null")),
            Objects.requireNonNull(entityInterface,
                Messages.getString("QueryManagerImpl.error_msg_iface_null")));
    }

    @Override
    public Query createQuery(SqlStatement statement) {
        return new QueryImpl(this.ds, Objects.requireNonNull(statement,
            Messages.getString("QueryManagerImpl.error_msg_statement_null")));
    }
}
