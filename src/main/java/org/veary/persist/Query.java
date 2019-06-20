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
 * Handles SQL statement which return 0 or more results..
 *
 * @author Marc L. Veary
 * @since 1.0
 */
public interface Query {

    /**
     * Execute this SELECT query.
     *
     * @return the current {@code Query} object
     */
    Query execute();

    /**
     * Returns an expected single result from the {@link #execute()}ing the SELECT query that
     * returns a single result.
     *
     * <p><b>Note:</b> the actual type of {@code Object} returned is an implementation of
     * interface passed to the constructor implementing this interface.
     *
     * @return {@link Object}
     */
    Object getSingleResult();

    /**
     * Returns the query's results as a List.
     *
     * @return unmodifiable {@link List}. Cannot be {@code null}
     */
    List<Object> getResultList();
}
