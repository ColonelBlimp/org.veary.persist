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

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Objects;

/**
 * Currently, the prupose of this interface is as a variable binding mechanism for SQL
 * statements.
 *
 * <p>In the future, this could be expanded to provide a basic query building framework.
 *
 * @author Marc L. Veary
 * @since 1.0
 */
public interface SqlBuilder {

    /**
     * Static factory method.
     *
     * @param sql {@code String}
     * @return an {@code SqlBuilder} instance
     */
    static SqlBuilder newInstance(String sql) {
        Objects.requireNonNull(sql, "String parameter cannot be null.");
        if ("".equals(sql)) {
            throw new IllegalArgumentException("String parameter must be non-empty.");
        }

        return new SqlBuilder() {
            @Override
            public String toString() {
                return sql;
            }
        };
    }

    /**
     * Returns a string representation of an SQL statement built by this builder. This can be
     * passed into methods such as {@link SqlStatement}, {@link Statement} or
     * {@link PreparedStatement}
     *
     * @return cannot return {@code null} or empty string
     */
    @Override
    String toString();
}
