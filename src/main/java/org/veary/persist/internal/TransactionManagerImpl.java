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
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.veary.persist.Statement;
import org.veary.persist.TransactionManager;
import org.veary.persist.exceptions.PersistenceException;

public final class TransactionManagerImpl implements TransactionManager {

    private final DataSource ds;
    private boolean txActive;
    private final List<Statement> statements;
    private int rowCountResult;
    private List<Integer> generatedIds;

    @Inject
    public TransactionManagerImpl(DataSource ds) {
        this.ds = ds;
        this.statements = new ArrayList<>();
    }

    @Override
    public void beginTransaction() {
        if (this.txActive) {
            throw new IllegalStateException("Transaction already active.");
        }

        this.generatedIds = new ArrayList<>();
        this.rowCountResult = 0;
        this.txActive = true;
    }

    @Override
    public void commitTransaction() {
        if (this.statements.isEmpty()) {
            throw new IllegalStateException("Nothing to commit.");
        }

        try (
            Connection conn = this.ds.getConnection();
            AutoSetAutoCommit auto = new AutoSetAutoCommit(conn);
            AutoRollback rb = new AutoRollback(conn)) {

            for (Statement statement : this.statements) {
                try (PreparedStatement pstmt = conn.prepareStatement(statement.getStatement(),
                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                    for (Map.Entry<Integer, Object> entry : statement.getParameters()
                        .entrySet()) {
                        pstmt.setObject(entry.getKey().intValue(), entry.getValue());
                    }

                    this.rowCountResult = pstmt.executeUpdate();

                    try (ResultSet rset = pstmt.getGeneratedKeys()) {
                        if (rset.isBeforeFirst()) {
                            while (rset.next()) {
                                this.generatedIds.add(Integer.valueOf(rset.getInt(1)));
                            }
                        }
                    }
                }
            }

        } catch (final SQLException e) {
            throw new PersistenceException(e);
        }

        this.txActive = false;
    }

    @Override
    public void persist(Statement statement) {
        if (!this.txActive) {
            throw new IllegalStateException(
                "No active transaction. Call TransactionManager.beginTransaction() first.");
        }
        this.statements.add(statement);
    }

    @Override
    public List<Integer> getGeneratedIds() {
        return this.generatedIds;
    }

    @Override
    public int getRowCount() {
        return this.rowCountResult;
    }
}
