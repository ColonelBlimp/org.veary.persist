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

import org.veary.persist.Transaction;
import org.veary.persist.TransactionManager;
import org.veary.persist.UpdateQuery;

public final class TransactionManagerImpl implements TransactionManager {

    private final DataSource ds;

    private Transaction transaction;

    @Inject
    public TransactionManagerImpl(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public Transaction getTransaction() {
        if (this.transaction == null) {
            this.transaction = new TransactionImpl(this.ds);
        }
        return this.transaction;
    }

    @Override
    public void persist(UpdateQuery updateQuery) {
    }
}
