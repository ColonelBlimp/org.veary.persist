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
import java.util.Map;
import java.util.Objects;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.veary.persist.SqlStatement;
import org.veary.persist.TransactionManager;
import org.veary.persist.exceptions.PersistenceException;

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
 * Long resultId = manager.persist(statement)
 * manager.commit();
 * </pre>
 *
 * @author Marc L. Veary
 * @since 1.0
 */
public final class TransactionManagerImpl implements TransactionManager {

    private static final Logger LOG = LogManager.getLogger(TransactionManagerImpl.class);
    private static final String SELECT_STR = "SELECT"; //$NON-NLS-1$

    private final DataSource ds;
    private boolean txActive;
    private boolean persistCalled;
    private int rowCountResult;
    private Connection conn;

    /**
     * Constructor.
     *
     * @param ds {@link DataSource}
     */
    @Inject
    public TransactionManagerImpl(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public void begin() {
        if (this.txActive || this.conn != null) {
            throw new IllegalStateException("Transaction already active.");
        }

        try {
            this.conn = this.ds.getConnection();
        } catch (SQLException e) {
            if (e.getCause() == null) {
                throw new PersistenceException(e);
            }
            throw new PersistenceException(e.getCause());
        }

        this.rowCountResult = 0;
        this.txActive = true;
        this.persistCalled = false;
    }

    @Override
    public void commit() {
        if (!this.txActive) {
            throw new IllegalStateException("No active transaction.");
        }

        if (!this.persistCalled) {
            throw new IllegalStateException("Nothing to commit.");
        }

        try {
            this.conn.commit();
            this.conn.setAutoCommit(true);
            this.conn.close();
        } catch (final SQLException e) {
            rollback();
            if (e.getCause() == null) {
                throw new PersistenceException(e);
            }
            throw new PersistenceException(e.getCause());
        } finally {
            this.conn = null;
        }

        this.txActive = false;
    }

    @Override
    public Long persist(SqlStatement statement) {
        if (!this.txActive) {
            throw new IllegalStateException("No active transaction.");
        }

        Objects.requireNonNull(statement, "Statement cannot be null.");
        if (statement.toString().toUpperCase().startsWith(SELECT_STR)) {
            throw new IllegalStateException(
                Messages.getString("QueryImpl.error_msg_incorrect_query_type")); //$NON-NLS-1$
        }

        Long id = Long.valueOf(0);
        try (PreparedStatement pstmt = this.conn.prepareStatement(statement.toString(),
            PreparedStatement.RETURN_GENERATED_KEYS)) {

            for (Map.Entry<Integer, Object> entry : statement.getParameters()
                .entrySet()) {
                pstmt.setObject(entry.getKey().intValue(), entry.getValue());
            }

            this.rowCountResult = pstmt.executeUpdate();

            id = getGeneratedKey(pstmt);
        } catch (SQLException e) {
            rollback();
            if (e.getCause() == null) {
                throw new PersistenceException(e);
            }
            throw new PersistenceException(e.getCause());
        }

        this.persistCalled = true;
        return id;
    }

    @Override
    public int getRowCount() {
        return this.rowCountResult;
    }

    @Override
    public boolean isActive() {
        return this.txActive;
    }

    /**
     * Returns a generated id.
     *
     * @param pstmt {@link PreparedStatement}
     * @return {@code Long} the generated id, otherwise 0. Cannot be {@code null}.
     * @throws SQLException if a database access error occurs
     */
    private Long getGeneratedKey(PreparedStatement pstmt) throws SQLException {
        try (ResultSet rset = pstmt.getGeneratedKeys()) {
            if (rset.isBeforeFirst() && rset.next()) {
                return Long.valueOf(rset.getInt(1));
            }
        }
        return Long.valueOf(0);
    }

    private void rollback() {
        try {
            this.conn.rollback();
            this.conn.close();
            this.conn = null;
        } catch (SQLException e) {
            LOG.error("Rollback failed: ", e);
        }
    }
}
