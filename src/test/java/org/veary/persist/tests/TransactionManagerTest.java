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
import org.veary.persist.SqlBuilder;
import org.veary.persist.SqlStatement;
import org.veary.persist.TransactionManager;
import org.veary.persist.internal.GuicePersistModule;

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
            new GuicePersistTestModule(),
            new GuicePersistModule());
    }

    @AfterClass
    public void teardown() {
        this.tomcatJndi.tearDown();
    }

    @Test
    public void processTransaction() {
        final TransactionManager manager = this.injector.getInstance(TransactionManager.class);
        Assert.assertNotNull(manager);

        manager.begin();

        SqlBuilder createTableBuilder = SqlBuilder.newInstance(
            "CREATE TABLE IF NOT EXISTS debs.account(id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255))");
        SqlStatement createTable = SqlStatement.newInstance(createTableBuilder);
        manager.persist(createTable);
        manager.commit();

        manager.begin();

        SqlBuilder insertAccount = SqlBuilder
            .newInstance("INSERT INTO debs.account(name) VALUES(?)");
        SqlStatement createAccountOne = SqlStatement.newInstance(insertAccount);
        createAccountOne.setParameter(1, "CASH");
        manager.persist(createAccountOne);

        SqlStatement createAccountTwo = SqlStatement.newInstance(insertAccount);
        createAccountTwo.setParameter(1, "EXPENSE");
        manager.persist(createAccountTwo);

        manager.commit();

        Assert.assertTrue(manager.getRowCount() == 1);
        Assert.assertTrue(manager.getGeneratedIdList().size() == 2);
    }

    @Test(
        expectedExceptions = IllegalStateException.class,
        expectedExceptionsMessageRegExp = "No active transaction.")
    public void exceptionCommitBeforeBegin() {
        final TransactionManager manager = this.injector.getInstance(TransactionManager.class);
        Assert.assertNotNull(manager);
        manager.persist(null);
    }

    @Test(
        expectedExceptions = NullPointerException.class,
        expectedExceptionsMessageRegExp = "Statement cannot be null.")
    public void exceptionNullStatement() {
        final TransactionManager manager = this.injector.getInstance(TransactionManager.class);
        Assert.assertNotNull(manager);
        manager.begin();
        manager.persist(null);
    }

    @Test(expectedExceptions = IllegalStateException.class,
        expectedExceptionsMessageRegExp = "Transaction already active.")
    public void activeTransactionException() {
        final TransactionManager manager = this.injector.getInstance(TransactionManager.class);
        Assert.assertNotNull(manager);
        manager.begin();
        manager.begin();
    }

    @Test(expectedExceptions = IllegalStateException.class,
        expectedExceptionsMessageRegExp = "No active transaction.")
    public void commitTxNotActiveException() {
        final TransactionManager manager = this.injector.getInstance(TransactionManager.class);
        Assert.assertNotNull(manager);
        manager.commit();
    }

    @Test(expectedExceptions = IllegalStateException.class,
        expectedExceptionsMessageRegExp = "Nothing to commit.")
    public void noStatementsException() {
        final TransactionManager manager = this.injector.getInstance(TransactionManager.class);
        Assert.assertNotNull(manager);
        manager.begin();
        manager.commit();
    }

    @Test(
        expectedExceptions = IllegalStateException.class,
        expectedExceptionsMessageRegExp = "Incorrect query type.")
    public void typeException() {
        final TransactionManager manager = this.injector.getInstance(TransactionManager.class);
        Assert.assertNotNull(manager);
        manager.begin();
        SqlBuilder select = SqlBuilder.newInstance("SELECT * FROM debs.account");
        SqlStatement find = SqlStatement.newInstance(select);
        manager.persist(find);
    }
}
