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
import java.sql.SQLException;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.veary.persist.QueryBuilder;
import org.veary.persist.Statement;
import org.veary.persist.Transaction;
import org.veary.persist.exceptions.PersistenceException;

public final class TransactionImpl implements Transaction {

    private static final Logger LOG = LogManager.getLogger(TransactionImpl.class);

    private final Connection conn;

    public TransactionImpl(Connection conn) {
        this.conn = Objects.requireNonNull(conn, "Transaction.Connection cannot be null.");
    }

    @Override
    public void begin() {
        try {
            if (this.conn.getAutoCommit()) {
                throw new IllegalStateException(
                    "Transaction.start the connection is set to auto-commit.");
            }
        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }
    }

    @Override
    public void commit() {
        try {
            this.conn.commit();
        } catch (final SQLException e) {
            doRollback();
            throw new PersistenceException(e);
        } finally {
            try {
                this.conn.close();
            } catch (final SQLException e) {
                LOG.error("Connection.close failed: {}", e);
            }
        }
    }

    @Override
    public Statement create(QueryBuilder queryBuilder) {
        return null;
    }

    private void doRollback() {
        try {
            this.conn.rollback();
        } catch (final SQLException e) {
            LOG.error("Connection.rollback failed: {}", e);
        }
    }
}
