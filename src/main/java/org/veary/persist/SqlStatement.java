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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a parameterized SQL statement used to populate a {@code PreparedStatement} object
 * internally.
 *
 * @author Marc L. Veary
 * @since 1.0
 */
public interface SqlStatement {

    /**
     * Sets the value of the designated parameter using the given object.
     *
     * @param index the first parameter is 1, the second is 2, ...
     * @param value the object containing the input parameter value
     * @return the value of the {@code SqlStatement} itself
     */
    SqlStatement setParameter(int index, Object value);

    /**
     * Returns the set parameters as a {@code Map<Integer, Object>}. The key indicates the index
     * position and the value the Object to be set.
     *
     * @return {@code Map<Integer, Object>}
     */
    Map<Integer, Object> getParameters();

    /**
     * Clears all previously set parameters.
     *
     * @return the value of the {@code SqlStatement} itself
     */
    SqlStatement clearParameters();

    /**
     * Static factory method for creating instances of this interface.
     *
     * @param statement DML/DDL statement with zero or more IN parameters
     * @return a new {@code SqlStatement} object
     */
    static SqlStatement newInstance(String statement) {
        Objects.requireNonNull(statement, "String parameter cannot be null.");
        if ("".equals(statement)) {
            throw new IllegalArgumentException("String parameter must be non-empty.");
        }

        return new SqlStatement() {

            private Map<Integer, Object> params = new HashMap<>();

            @Override
            public SqlStatement setParameter(int index, Object value) {
                if (index < 1) {
                    throw new IllegalArgumentException("Parameter index starts a 1.");
                }
                this.params.put(Integer.valueOf(index),
                    Objects.requireNonNull(value, "Object parameter is null."));
                return this;
            }

            /**
             * Returns an SQL statement that may contain zero or more '?' IN parameter
             * placeholders.
             *
             * @return {@code String}
             */
            @Override
            public String toString() {
                return statement;
            }

            @Override
            public Map<Integer, Object> getParameters() {
                return Collections.unmodifiableMap(this.params);
            }

            @Override
            public SqlStatement clearParameters() {
                this.params = new HashMap<>();
                return this;
            }
        };
    }
}
