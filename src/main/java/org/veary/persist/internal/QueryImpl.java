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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.veary.persist.Query;
import org.veary.persist.entity.Entity;
import org.veary.persist.exceptions.PersistenceException;

public final class QueryImpl implements Query {

    private final Map<String, Object> parameters;
    private final String nativeSql;
    private final Class<? extends Entity> entityInterface;
    private Map<String, Object> result;

    public QueryImpl(String nativeSql, Class<? extends Entity> entityInterface) {
        this.nativeSql = nativeSql;
        this.entityInterface = entityInterface;
        this.parameters = new HashMap<>();
    }

    @Override
    public Object getSingleResult() {
        return getNewInstance(getStaticFactoryMethod(), this.result);
    }

    @Override
    public List<? extends Entity> getResultList() {
        return null;
    }

    @Override
    public Query setParameter(int index, Object value) {
        if (index < 1) {
            throw new IllegalArgumentException("Query parameter index starts a 1");
        }
        this.parameters.put(String.valueOf(index), value);
        return this;
    }

    @Override
    public Query executeQuery() {
        return this;
    }

    @Override
    public int executeUpdate() {
        return 0;
    }

    private Method getStaticFactoryMethod() {
        try {
            return this.entityInterface.getDeclaredMethod("newInstance", Map.class);
        } catch (NoSuchMethodException | SecurityException e) {
            throw new PersistenceException(e);
        }
    }

    private Object getNewInstance(Method staticFactory, Map<String, Object> result) {
        try {
            return staticFactory.invoke(entityInterface, result);
        } catch (IllegalAccessException | IllegalArgumentException
            | InvocationTargetException e) {
            throw new PersistenceException(e);
        }
    }
}
