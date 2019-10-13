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
import org.veary.persist.SqlStatement;
import org.veary.persist.TransactionManager;

import hthurow.tomcatjndi.TomcatJNDI;

public class TransactionManagerTest {

    private TomcatJNDI tomcatJndi;
    private Injector injector;

    @BeforeClass
    public void setUp() {
        final File contextXml = new File("src/test/resources/context.xml");
        this.tomcatJndi = new TomcatJNDI();
        this.tomcatJndi.processContextXml(contextXml);
        this.tomcatJndi.start();
        this.injector = Guice.createInjector(
            new GuicePersistTestModule());
    }

    @AfterClass
    public void teardown() {
        this.tomcatJndi.tearDown();
    }

    @Test
    public void processTransaction() {
        final PersistenceManagerFactory factory = this.injector
            .getInstance(PersistenceManagerFactory.class);
        final TransactionManager manager = factory.createTransactionManager();
        Assert.assertNotNull(manager);

        SqlStatement createTable = SqlStatement.newInstance(
            "CREATE TABLE IF NOT EXISTS debs.account(id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255))");
        manager.begin();
        Long result = manager.persist(createTable);
        Assert.assertEquals(result, Long.valueOf(0));
        manager.commit();

        manager.begin();

        SqlStatement createAccountOne = SqlStatement
            .newInstance("INSERT INTO debs.account(name) VALUES(?)");
        createAccountOne.setParameter(1, "CASH");
        result = manager.persist(createAccountOne);
        Assert.assertTrue(result.longValue() > 0);

        SqlStatement createAccountTwo = SqlStatement
            .newInstance("INSERT INTO debs.account(name) VALUES(?)");
        createAccountTwo.setParameter(1, "EXPENSE");
        result = manager.persist(createAccountTwo);
        Assert.assertTrue(result.longValue() > 0);

        manager.commit();

        Assert.assertTrue(manager.getRowCount() == 1);
    }

    @Test(
        expectedExceptions = IllegalStateException.class,
        expectedExceptionsMessageRegExp = "No active transaction.")
    public void exceptionCommitBeforeBegin() {
        final PersistenceManagerFactory factory = this.injector
            .getInstance(PersistenceManagerFactory.class);
        final TransactionManager manager = factory.createTransactionManager();
        Assert.assertNotNull(manager);
        manager.persist(null);
    }

    @Test(
        expectedExceptions = NullPointerException.class,
        expectedExceptionsMessageRegExp = "Statement cannot be null.")
    public void exceptionNullStatement() {
        final PersistenceManagerFactory factory = this.injector
            .getInstance(PersistenceManagerFactory.class);
        final TransactionManager manager = factory.createTransactionManager();
        Assert.assertNotNull(manager);
        manager.begin();
        manager.persist(null);
    }

    @Test(expectedExceptions = IllegalStateException.class,
        expectedExceptionsMessageRegExp = "Transaction already active.")
    public void activeTransactionException() {
        final PersistenceManagerFactory factory = this.injector
            .getInstance(PersistenceManagerFactory.class);
        final TransactionManager manager = factory.createTransactionManager();
        Assert.assertNotNull(manager);
        manager.begin();
        manager.begin();
    }

    @Test(expectedExceptions = IllegalStateException.class,
        expectedExceptionsMessageRegExp = "No active transaction.")
    public void commitTxNotActiveException() {
        final PersistenceManagerFactory factory = this.injector
            .getInstance(PersistenceManagerFactory.class);
        final TransactionManager manager = factory.createTransactionManager();
        Assert.assertNotNull(manager);
        manager.commit();
    }

    @Test(expectedExceptions = IllegalStateException.class,
        expectedExceptionsMessageRegExp = "Nothing to commit.")
    public void noStatementsException() {
        final PersistenceManagerFactory factory = this.injector
            .getInstance(PersistenceManagerFactory.class);
        final TransactionManager manager = factory.createTransactionManager();
        Assert.assertNotNull(manager);
        manager.begin();
        manager.commit();
    }

    @Test(
        expectedExceptions = IllegalStateException.class,
        expectedExceptionsMessageRegExp = "Incorrect query type.")
    public void typeException() {
        final PersistenceManagerFactory factory = this.injector
            .getInstance(PersistenceManagerFactory.class);
        final TransactionManager manager = factory.createTransactionManager();
        Assert.assertNotNull(manager);
        manager.begin();
        SqlStatement find = SqlStatement.newInstance("SELECT * FROM debs.account");
        manager.persist(find);
    }
}
