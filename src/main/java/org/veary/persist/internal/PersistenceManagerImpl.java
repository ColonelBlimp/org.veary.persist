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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.veary.persist.PersistenceManager;
import org.veary.persist.Query;
import org.veary.persist.entity.Entity;

public final class PersistenceManagerImpl implements PersistenceManager {

    private static final Logger LOG = LogManager.getLogger(PersistenceManagerImpl.class);

    private final DataSource dataSource;

    @Inject
    public PersistenceManagerImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Query createQuery(String nativeSql, Class<? extends Entity> entityInterface) {
        return new QueryImpl(this.dataSource, nativeSql, entityInterface);
    }

	@Override
	public Query createQuery(String nativeSql) {
		return new QueryImpl(this.dataSource, nativeSql);
	}
}
