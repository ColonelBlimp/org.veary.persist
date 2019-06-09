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

package org.veary.persist;

/**
 * <b>Purpose:</b> Defines the interface for the libray's entry point.
 *
 * @author Marc L. Veary
 * @since 1.0
 */
public interface PersistenceManager {

    /**
     * Creates a {@code Query} object initialized with the SQL statement provided by the given
     * {@code QueryBuilder}.
     *
     * @param builder {@link QueryBuilder}
     * @return {@link Query}
     */
    Query createQuery(QueryBuilder builder);

    /**
     * Creates a {@code Query} object initialized with the SQL statement provided by the given
     * {@code QueryBuilder}.
     *
     * @param builder {@link QueryBuilder}
     * @param entityInterface An Interface class which extends {@link Entity} the interface
     * @return {@link Query}
     */
    Query createQuery(QueryBuilder builder, Class<? extends Entity> entityInterface);

    /**
     * Sets this connection's auto-commit mode to {@code false}. SQL statements are grouped into
     * transactions that are auto-committed when the code exits, or auto-rolledback if an
     * exception is thrown.
     *
     * <p>This method should be called <b>before</b> a call to {@link #createQuery}. If called
     * after a {@code createQuery}, the call is no-op.
     *
     * @param autoCommit {@code true} to enable auto-commit mode; {@code false} to disable it
     */
    void processAsTransaction();
}
