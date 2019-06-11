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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.veary.persist.SqlStatement;
import org.veary.persist.TransactionManager;
import org.veary.persist.exceptions.PersistenceException;

/**
 * Concrete implementation of the {@link TransactionManager} interface.
 *
 * @author Marc L. Veary
 * @since 1.0
 */
public final class TransactionManagerImpl implements TransactionManager {

    private final DataSource ds;
    private boolean txActive;
    private final List<SqlStatement> statements;
    private int rowCountResult;
    private List<Integer> generatedIds;

    /**
     * Constructor.
     *
     * @param ds {@link DataSource}
     */
    @Inject
    public TransactionManagerImpl(DataSource ds) {
        this.ds = ds;
        this.statements = new ArrayList<>();
        this.generatedIds = Collections.emptyList();
    }

    @Override
    public void begin() {
        if (this.txActive) {
            throw new IllegalStateException("Transaction already active.");
        }

        this.generatedIds = new ArrayList<>();
        this.rowCountResult = 0;
        this.txActive = true;
    }

    @Override
    public void commit() {
        if (!this.txActive) {
            throw new IllegalStateException("No active transaction.");
        }
        if (this.statements.isEmpty()) {
            throw new IllegalStateException("Nothing to commit.");
        }

        try (
            Connection conn = this.ds.getConnection();
            AutoSetAutoCommit auto = new AutoSetAutoCommit(conn);
            AutoRollback tx = new AutoRollback(conn)) {

            createStatementAndExecute(conn);

            tx.commit();

        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }

        this.txActive = false;
    }

    @Override
    public void persist(SqlStatement statement) {
        if (!this.txActive) {
            throw new IllegalStateException("No active transaction.");
        }
        this.statements.add(Objects.requireNonNull(statement, "Statement cannot be null."));
    }

    @Override
    public List<Integer> getGeneratedIdList() {
        return this.generatedIds;
    }

    @Override
    public int getRowCount() {
        return this.rowCountResult;
    }

    /**
     * Creates a {@link PreparedStatement}, calls the {@link PreparedStatement#executeUpdate()}
     * and then process the generated keys (if there are any).
     *
     * @param conn {@link Connection}
     * @throws SQLException if a database access error occurs
     * @see #setGeneratedKeys(PreparedStatement)
     */
    private void createStatementAndExecute(Connection conn) throws SQLException {
        for (SqlStatement statement : this.statements) {
            try (PreparedStatement pstmt = conn.prepareStatement(statement.getStatement(),
                PreparedStatement.RETURN_GENERATED_KEYS)) {

                this.rowCountResult = pstmt.executeUpdate();

                setGeneratedKeys(pstmt);
            }
        }
    }

    /**
     * Copies any generated keys from the designated {@code PreparedStatement} to a class member
     * variable {@code List} which allows can be accessed.
     *
     * @param pstmt {@link PreparedStatement}
     * @throws SQLException if a database access error occurs
     */
    private void setGeneratedKeys(PreparedStatement pstmt) throws SQLException {
        try (ResultSet rset = pstmt.getGeneratedKeys()) {
            if (rset.isBeforeFirst()) {
                while (rset.next()) {
                    this.generatedIds.add(Integer.valueOf(rset.getInt(1)));
                }
            }
        }
    }
}
