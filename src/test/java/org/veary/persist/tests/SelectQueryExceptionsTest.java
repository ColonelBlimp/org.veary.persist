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

package org.veary.persist.tests;

import java.io.File;

import javax.swing.text.html.parser.Entity;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.veary.persist.QueryBuilder;
import org.veary.persist.QueryManager;
import org.veary.persist.SelectQuery;
import org.veary.persist.internal.GuicePersistModule;
import org.veary.persist.internal.SelectQueryImpl;

import com.google.inject.Guice;
import com.google.inject.Injector;

import hthurow.tomcatjndi.TomcatJNDI;

public class SelectQueryExceptionsTest {

    private TomcatJNDI tomcatJndi;
    private Injector injector;

    @BeforeClass
    public void setUp() {
        final File contextXml = new File("src/test/resources/context.xml");
        this.tomcatJndi = new TomcatJNDI();
        this.tomcatJndi.processContextXml(contextXml);
        this.tomcatJndi.start();
        this.injector = Guice.createInjector(
            new GuicePersistTestModule(),
            new GuicePersistModule());
    }

    @AfterClass
    public void teardown() {
        this.tomcatJndi.tearDown();
    }

    @Test(
        expectedExceptions = NullPointerException.class,
        expectedExceptionsMessageRegExp = "SelectQuery.Class<\\?> parameter is null.")
    public void constructorNullInterfaceException() {
        final QueryManager manager = this.injector.getInstance(QueryManager.class);
        Assert.assertNotNull(manager);
        manager.createQuery(QueryBuilder.newInstance("SELECT * FROM ?"), null);
    }

    @Test(
        expectedExceptions = NullPointerException.class,
        expectedExceptionsMessageRegExp = "SelectQuery.QueryBuilder parameter is null.")
    public void constructorNullQueryBuilderException() {
        final QueryManager manager = this.injector.getInstance(QueryManager.class);
        Assert.assertNotNull(manager);
        manager.createQuery(null, null);
    }

    @Test(
        expectedExceptions = NullPointerException.class,
        expectedExceptionsMessageRegExp = "SelectQuery.DataSource parameter is null.")
    public void constructorNullDataSourceException() {
        new SelectQueryImpl(null, null, null);
    }

    @Test(
        expectedExceptions = IllegalArgumentException.class,
        expectedExceptionsMessageRegExp = "SelectQuery.setParameter index starts a 1.")
    public void constructorSetParameterInvalidIndexException() {
        final QueryManager manager = this.injector.getInstance(QueryManager.class);
        Assert.assertNotNull(manager);
        final SelectQuery query = manager.createQuery(QueryBuilder.newInstance("SELECT * FROM ?"),
            Entity.class);
        Assert.assertNotNull(query);
        query.setParameter(0, "VALUE");
    }

    @Test(
        expectedExceptions = NullPointerException.class,
        expectedExceptionsMessageRegExp = "SelectQuery.setParameter.Object parameter is null.")
    public void constructorSetParameterNullValueException() {
        final QueryManager manager = this.injector.getInstance(QueryManager.class);
        Assert.assertNotNull(manager);
        final SelectQuery query = manager.createQuery(QueryBuilder.newInstance("SELECT * FROM ?"),
            Entity.class);
        Assert.assertNotNull(query);
        query.setParameter(1, null);
    }

    @Test(
        expectedExceptions = IllegalStateException.class,
        expectedExceptionsMessageRegExp = "SelectQuery.execute incorrect query type. Use SELECT.")
    public void constructorExecuteTypeException() {
        final QueryManager manager = this.injector.getInstance(QueryManager.class);
        Assert.assertNotNull(manager);
        final SelectQuery query = manager.createQuery(QueryBuilder.newInstance("INSERT INTO"),
            Entity.class);
        Assert.assertNotNull(query);
        query.execute();
    }
}
