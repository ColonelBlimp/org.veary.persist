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
 * <b>Purpose:</b> defines the methods for using JDBC transactions.
 *
 * @author Marc L. Veary
 * @since 1.0
 */
public interface TransactionManager {

    /**
     * Mark the start of a transaction.
     */
    void begin();

    /**
     * Commits all the persisted sql statements.
     */
    void commit();

    /**
     * Persists the designated {@code SqlStatement} to the JDBC driver.
     *
     * @param statement {@link SqlStatement}
     * @return {@code Long} the value of the generated Id, otherwise 0
     */
    Long persist(SqlStatement statement);

    /**
     * Returns the row count for SQL Data Manipulation Language (DML) statements, or 0 for SQL
     * statements that return nothing.
     *
     * @return int
     */
    int getRowCount();

    /**
     * Tests if there is a currently active transaction.
     *
     * @return {@code true} if there is a currently active transaction, otherwise {@code false}
     */
    boolean isActive();
}
