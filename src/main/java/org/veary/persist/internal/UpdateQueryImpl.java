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
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.veary.persist.QueryBuilder;
import org.veary.persist.UpdateQuery;
import org.veary.persist.exceptions.PersistenceException;

public final class UpdateQueryImpl implements UpdateQuery {

    private static final Logger LOG = LogManager.getLogger(UpdateQueryImpl.class);

    private final PreparedStatement stmt;

    public UpdateQueryImpl(Connection conn, QueryBuilder queryBuilder) {
        try {
            this.stmt = conn.prepareStatement(queryBuilder.toString(),
                PreparedStatement.RETURN_GENERATED_KEYS);
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public UpdateQuery setParameter(int index, Object value) {
        try {
            this.stmt.setObject(index, value);
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
        return this;
    }

    @Override
    public int persist() {
        int result = 0;

        try {
            result = this.stmt.executeUpdate();
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        } finally {
            try {
                this.stmt.close();
            } catch (final SQLException e) {
                LOG.error("UpdateQuery.persist: PreparedStatement.close error {}", e);
            }
        }

        return result;
    }
}
