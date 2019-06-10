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

import java.util.List;

/**
 * <h2>Purpose:</h2> handles transactions through JDBC.
 *
 * <h2>Usage:</h2>
 *
 * <pre>
 * SqlBuilder builder = SqlBuilder.newInstance("INSERT INTO schema.table(name) VALUES(?)");
 * SqlStatement statement = SqlStatement.newInstance(builder);
 * statement.setParameter(1, "CASH");
 *
 * TransactionManager manager = injector.getInstance(TransactionManager.class);
 * manager.begin();
 * manager.persist(statement)
 * manager.commit();
 * </pre>
 *
 * The following two method will return the results for a call to {@code commit()}:
 *
 * <pre>
 * int rowCount = manager.getRowCount();
 * List<Integer> ids = manager.getGeneratedIdList();
 * </pre>
 *
 * <p>However, the results of a call to {@code commit()} are reset upon a call to
 * {@code begin()}. <h2>Rollback:</h2>
 *
 * <p>The manager automaticall handles rollback if there is an error.
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
     * Persists the designated {@code SqlStatement} to the JDBC driver.
     *
     * @param statement {@link SqlStatement}
     */
    void persist(SqlStatement statement);

    /**
     * Commits all the persisted
     */
    void commit();

    /**
     * Returns a {@code List<Integer>} of the generated ids from the transaction. The id's are in
     * the same order as calls to {@code persis(...)}.
     *
     * @return {@code List<Integer>}
     */
    List<Integer> getGeneratedIdList();

    /**
     * Returns the row count for SQL Data Manipulation Language (DML) statements, or 0 for SQL
     * statements that return nothing.
     *
     * @return int
     */
    int getRowCount();
}
