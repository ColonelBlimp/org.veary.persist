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

/**
 * Resets {@code Connection#setAutoCommit(boolean)} to its original state. This used with a
 * {@code try-with-resource} syntax.
 *
 * @author Marc L. Veary
 * @since 1.0
 */
public final class AutoSetAutoCommit implements AutoCloseable {

    private final Connection conn;
    private final boolean originalAutoCommit;

    /**
     * Constructor.
     * 
     * @param conn {@link Connection}
     * @param autoCommit {@code boolean}
     * @throws SQLException if there is an error
     */
    protected AutoSetAutoCommit(Connection conn, boolean autoCommit) throws SQLException {
        this.conn = conn;
        this.originalAutoCommit = conn.getAutoCommit();
        this.conn.setAutoCommit(autoCommit);
    }

    @Override
    public void close() throws SQLException {
        this.conn.setAutoCommit(this.originalAutoCommit);
    }
}
