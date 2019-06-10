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

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.veary.persist.Entity;
import org.veary.persist.QueryManager;
import org.veary.persist.SqlBuilder;
import org.veary.persist.internal.GuicePersistModule;
import org.veary.persist.internal.QueryManagerImpl;

import com.google.inject.Guice;
import com.google.inject.Injector;

import hthurow.tomcatjndi.TomcatJNDI;

public class QueryManagerTest {

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

    @Test
    public void createQuery() {
        final QueryManager manager = this.injector.getInstance(QueryManager.class);
        Assert.assertNotNull(manager);

        Assert.assertNotNull(
            manager.createQuery(SqlBuilder.newInstance("SELECT * FROM ?"), Entity.class));
    }

    @Test(
        expectedExceptions = NullPointerException.class,
        expectedExceptionsMessageRegExp = "DataSource parameter is null.")
    public void queryManagerNullDataSourceException() {
        new QueryManagerImpl(null);
    }

    @Test(
        expectedExceptions = NullPointerException.class,
        expectedExceptionsMessageRegExp = "SqlBuilder parameter is null.")
    public void queryManagerNullBuilder() {
        final QueryManager manager = this.injector.getInstance(QueryManager.class);
        Assert.assertNotNull(manager);
        manager.createQuery(null, null);
    }

    @Test(
        expectedExceptions = NullPointerException.class,
        expectedExceptionsMessageRegExp = "Class interface parameter is null.")
    public void queryManagerNullInterface() {
        final QueryManager manager = this.injector.getInstance(QueryManager.class);
        Assert.assertNotNull(manager);
        manager.createQuery(SqlBuilder.newInstance("SELECT * FROM ?"), null);
    }
}
