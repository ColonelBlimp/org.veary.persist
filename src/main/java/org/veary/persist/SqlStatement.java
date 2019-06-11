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
     * Returns an SQL statement that may contain one or more '?' IN parameter placeholders.
     *
     * @return {@code String}
     */
    String getStatement();

    /**
     * Returns the set parameters as a {@code Map<Integer, Object>}. The key indicates the index
     * position and the value the Object to be set.
     *
     * @return {@code Map<Integer, Object>}
     */
    Map<Integer, Object> getParameters();

    /**
     * Static factory method for creating instances of this interface.
     *
     * @param builder {@link SqlBuilder} object
     * @return a new {@code SqlStatement} object
     */
    static SqlStatement newInstance(SqlBuilder builder) {
        final String stmt = builder.toString();

        return new SqlStatement() {

            private String statement = stmt;
            private Map<Integer, Object> params = new HashMap<>();

            @Override
            public SqlStatement setParameter(int index, Object value) {
                this.params.put(Integer.valueOf(index), value);
                return this;
            }

            @Override
            public String getStatement() {
                return this.statement;
            }

            @Override
            public Map<Integer, Object> getParameters() {
                return Collections.unmodifiableMap(this.params);
            }
        };
    }
}
