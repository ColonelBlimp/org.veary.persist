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

package org.veary.persist.exceptions;

public class PersistenceException extends RuntimeException {

    private static final long serialVersionUID = -18289422949183361L;

    /**
     * Constructs a new {@code PersistenceException} exception with {@code null} as
     * its detail message.
     */
    public PersistenceException() {
        super();
    }

    /**
     * Constructs a new {@code PersistenceException} exception with the specified
     * detail message.
     *
     * @param message the detail message.
     */
    public PersistenceException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code PersistenceException} exception with the specified
     * detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the cause.
     */
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@code PersistenceException} exception with the specified
     * cause.
     *
     * @param cause the cause.
     */
    public PersistenceException(Throwable cause) {
        super(cause);
    }
}
