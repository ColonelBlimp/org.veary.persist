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

import javax.inject.Inject;
import javax.sql.DataSource;

import org.veary.persist.Entity;
import org.veary.persist.PersistenceManager;
import org.veary.persist.Query;
import org.veary.persist.QueryBuilder;

/**
 * <b>Purpose:</b> Concrete implementation of {@link PersistenceManager} interface. This class
 * is the entry point for the library.
 *
 * @author Marc L. Veary
 * @since 1.0
 */
public final class PersistenceManagerImpl implements PersistenceManager {

    private final DataSource dataSource;
    private boolean isAutoCommit;
    private boolean isActive;

    /**
     * Constructor. The default connection auto-commit status is:
     *
     * <pre>
     * Connection.getAutoCommit() == true
     * </pre>
     *
     * <p>However, if call to {@link #processAsTransaction()} is made before calling either
     * {@link #createQuery} method, the auto-commit status will be set to:
     *
     * <pre>
     * Connection.getAutoCommit() == false
     * </pre>
     *
     * @param dataSource {@link DataSource}
     */
    @Inject
    public PersistenceManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
        this.isAutoCommit = true;
        this.isActive = false;
    }

    @Override
    public Query createQuery(QueryBuilder builder, Class<? extends Entity> entityInterface) {
        this.isActive = true;
        return new QueryImpl(this.dataSource, this.isAutoCommit, builder, entityInterface);
    }

    @Override
    public Query createQuery(QueryBuilder builder) {
        this.isActive = true;
        return new QueryImpl(this.dataSource, this.isAutoCommit, builder);
    }

    @Override
    public void processAsTransaction() {
        if (this.isActive) {
            return;
        }
        this.isAutoCommit = false;
    }
}
