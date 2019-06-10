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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Automatically rollsback if the transaction has not been committed. This used with a
 * {@code try-with-resource} syntax.
 *
 * @author Marc L. Veary
 * @since 1.0
 */
public final class AutoRollback implements AutoCloseable {

    private static final Logger LOG = LogManager.getLogger(AutoRollback.class);
    private final Connection conn;
    private boolean committed;

    /**
     * Constructor.
     *
     * @param conn {@code Connection}
     */
    protected AutoRollback(Connection conn) {
        this.conn = conn;
    }

    /**
     * Wrapper around {@link Connection#commit()}.
     *
     * @throws SQLException if there is a problem
     */
    public void commit() throws SQLException {
        if (this.conn.getAutoCommit()) {
            throw new IllegalStateException(
                "Connection set to auto-commit.");
        }
        this.conn.commit();
        this.committed = true;
    }

    @Override
    public void close() throws SQLException {
        if (!this.committed) {
            this.conn.rollback();
            LOG.error("Transaction rolled back.");
        }
    }
}
