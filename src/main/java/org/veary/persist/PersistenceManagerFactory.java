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

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

import org.veary.persist.internal.QueryManagerImpl;
import org.veary.persist.internal.TransactionManagerImpl;

/**
 * <b>Purpose:</b> factory for all the manager classes.
 *
 * <p><b>Notes:</b> annotated for JSR 330
 *
 * @author Marc L. Veary
 * @since 1.0
 * @see QueryManager
 * @see TransactionManager
 * @see CallableManager
 */
@Singleton
public final class PersistenceManagerFactory {

    private final DataSource ds;

    /**
     * Constructor.
     *
     * @param ds {@code DataSource}
     */
    @Inject
    public PersistenceManagerFactory(DataSource ds) {
        this.ds = ds;
    }

    /**
     * Returns a new instance of the {@link QueryManager}.
     *
     * @return new instance of {@link QueryManager}
     */
    public QueryManager createQueryManager() {
        return new QueryManagerImpl(this.ds);
    }

    /**
     * Returns a new instance of the {@link TransactionManager}.
     *
     * @return new instance of {@link TransactionManager}
     */
    public TransactionManager createTransactionManager() {
        return new TransactionManagerImpl(this.ds);
    }

    /**
     * Returns a new instance of the {@link CallableManager}.
     *
     * @return new instance of {@link CallableManager}
     */
    public CallableManager createCallableManager() {
        throw new UnsupportedOperationException();
    }
}
