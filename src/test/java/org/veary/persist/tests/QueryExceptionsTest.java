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

import com.google.inject.Guice;
import com.google.inject.Injector;

import java.io.File;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.veary.persist.PersistenceManagerFactory;
import org.veary.persist.Query;
import org.veary.persist.QueryManager;
import org.veary.persist.SqlStatement;
import org.veary.persist.TransactionManager;
import org.veary.persist.exceptions.NoResultException;
import org.veary.persist.exceptions.PersistenceException;
import org.veary.persist.internal.QueryImpl;

import hthurow.tomcatjndi.TomcatJNDI;

public class QueryExceptionsTest {

    private TomcatJNDI tomcatJndi;
    private Injector injector;

    @BeforeClass
    public void setUp() {
        final File contextXml = new File("src/test/resources/context.xml");
        this.tomcatJndi = new TomcatJNDI();
        this.tomcatJndi.processContextXml(contextXml);
        this.tomcatJndi.start();
        this.injector = Guice.createInjector(new GuicePersistTestModule());
    }

    @AfterClass
    public void teardown() {
        this.tomcatJndi.tearDown();
    }

    @Test(
        expectedExceptions = NullPointerException.class,
        expectedExceptionsMessageRegExp = "Class interface parameter is null.")
    public void constructorNullInterfaceException() {
        final PersistenceManagerFactory factory = this.injector
            .getInstance(PersistenceManagerFactory.class);
        final QueryManager manager = factory.createQueryManager();
        Assert.assertNotNull(manager);
        manager.createQuery(SqlStatement.newInstance("SELECT * FROM ?"), null);
    }

    @Test(
        expectedExceptions = NullPointerException.class,
        expectedExceptionsMessageRegExp = "SqlStatement parameter is null.")
    public void constructorNullQueryBuilderException() {
        final PersistenceManagerFactory factory = this.injector
            .getInstance(PersistenceManagerFactory.class);
        final QueryManager manager = factory.createQueryManager();
        Assert.assertNotNull(manager);
        manager.createQuery(null, null);
    }

    @Test(
        expectedExceptions = NullPointerException.class,
        expectedExceptionsMessageRegExp = "DataSource parameter is null.")
    public void constructorNullDataSourceException() {
        new QueryImpl(null, null, null);
    }

    @Test(
        expectedExceptions = IllegalArgumentException.class,
        expectedExceptionsMessageRegExp = "Parameter index starts a 1.")
    public void constructorSetParameterInvalidIndexException() {
        final PersistenceManagerFactory factory = this.injector
            .getInstance(PersistenceManagerFactory.class);
        final QueryManager manager = factory.createQueryManager();
        Assert.assertNotNull(manager);

        SqlStatement statement = SqlStatement.newInstance("SELECT * FROM ?");
        statement.setParameter(0, "VALUE");

        final Query query = manager.createQuery(statement, String.class);
        Assert.assertNotNull(query);
    }

    @Test(
        expectedExceptions = NullPointerException.class,
        expectedExceptionsMessageRegExp = "Object parameter is null.")
    public void constructorSetParameterNullValueException() {
        final PersistenceManagerFactory factory = this.injector
            .getInstance(PersistenceManagerFactory.class);
        final QueryManager manager = factory.createQueryManager();
        Assert.assertNotNull(manager);

        SqlStatement statement = SqlStatement.newInstance("SELECT * FROM ?");
        statement.setParameter(1, null);

        final Query query = manager.createQuery(statement, String.class);
        Assert.assertNotNull(query);
    }

    @Test(
        expectedExceptions = IllegalStateException.class,
        expectedExceptionsMessageRegExp = "Incorrect query type.")
    public void constructorExecuteTypeException() {
        final PersistenceManagerFactory factory = this.injector
            .getInstance(PersistenceManagerFactory.class);
        final QueryManager manager = factory.createQueryManager();
        Assert.assertNotNull(manager);

        SqlStatement statement = SqlStatement.newInstance("INSERT INTO");
        final Query query = manager.createQuery(statement, String.class);
        Assert.assertNotNull(query);
        query.execute();
    }

    @Test(
        expectedExceptions = NoResultException.class,
        expectedExceptionsMessageRegExp = "Query did not return any results.")
    public void constructorNoResultsException() {
        final PersistenceManagerFactory factory = this.injector
            .getInstance(PersistenceManagerFactory.class);
        final TransactionManager txManager = factory.createTransactionManager();
        Assert.assertNotNull(txManager);

        SqlStatement createTable = SqlStatement.newInstance(
            "CREATE TABLE IF NOT EXISTS debs.account(id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255))");

        txManager.begin();
        txManager.persist(createTable);
        txManager.commit();

        final QueryManager manager = factory.createQueryManager();
        Assert.assertNotNull(manager);

        SqlStatement statement = SqlStatement
            .newInstance("SELECT * FROM debs.account WHERE id=?");
        statement.setParameter(1, Integer.valueOf(10));

        final Query query = manager.createQuery(statement, String.class);
        Assert.assertNotNull(query);
        query.execute();
    }

    @Test(
        expectedExceptions = PersistenceException.class,
        expectedExceptionsMessageRegExp = "Invalid method call sequence.")
    public void singleResultSequenceException() {
        final PersistenceManagerFactory factory = this.injector
            .getInstance(PersistenceManagerFactory.class);
        final QueryManager manager = factory.createQueryManager();
        Assert.assertNotNull(manager);

        SqlStatement statement = SqlStatement
            .newInstance("SELECT * FROM debs.account WHERE id=?");
        statement.setParameter(1, Integer.valueOf(10));

        final Query query = manager.createQuery(statement, String.class);
        Assert.assertNotNull(query);
        query.getSingleResult();
    }

    @Test(
        expectedExceptions = PersistenceException.class,
        expectedExceptionsMessageRegExp = "Invalid method call sequence.")
    public void listResultSequenceException() {
        final PersistenceManagerFactory factory = this.injector
            .getInstance(PersistenceManagerFactory.class);
        final QueryManager manager = factory.createQueryManager();
        Assert.assertNotNull(manager);
        SqlStatement statement = SqlStatement.newInstance("SELECT * FROM debs.account");

        final Query query = manager.createQuery(statement, String.class);
        Assert.assertNotNull(query);
        query.getResultList();
    }
}
