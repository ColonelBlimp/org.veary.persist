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
import java.util.List;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.veary.persist.PersistenceManagerFactory;
import org.veary.persist.Query;
import org.veary.persist.QueryManager;
import org.veary.persist.SqlStatement;
import org.veary.persist.TransactionManager;

import hthurow.tomcatjndi.TomcatJNDI;

public class QueryTest {

    private TomcatJNDI tomcatJndi;
    private Injector injector;
    private Long id;

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

    @Test
    public void createTables() {
        final PersistenceManagerFactory factory = this.injector
            .getInstance(PersistenceManagerFactory.class);

        final TransactionManager txManager = factory.createTransactionManager();
        Assert.assertNotNull(txManager);
        txManager.begin();
        SqlStatement createTable = SqlStatement.newInstance(
            "CREATE TABLE IF NOT EXISTS DEBS.ACCOUNT(ID INT PRIMARY KEY AUTO_INCREMENT, NAME VARCHAR(255))");
        Long idOne = txManager.persist(createTable);
        Assert.assertNotNull(idOne);
        Assert.assertTrue(idOne.longValue() == 0);

        SqlStatement insertAccountOne = SqlStatement
            .newInstance("INSERT INTO DEBS.ACCOUNT(NAME) VALUES(?)");
        insertAccountOne.setParameter(1, "CASH");
        this.id = txManager.persist(insertAccountOne);
        Assert.assertTrue(this.id.longValue() > 0);

        SqlStatement insertAccountTwo = SqlStatement
            .newInstance("INSERT INTO DEBS.ACCOUNT(NAME) VALUES(?)");
        insertAccountTwo.setParameter(1, "EXPENSE");
        Long idThree = txManager.persist(insertAccountTwo);
        Assert.assertNotNull(idThree);
        Assert.assertTrue(idThree.longValue() > 0);

        txManager.commit();
    }

    @Test
    public void singleResult() {
        final PersistenceManagerFactory factory = this.injector
            .getInstance(PersistenceManagerFactory.class);
        final QueryManager manager = factory.createQueryManager();
        Assert.assertNotNull(manager);

        SqlStatement statement = SqlStatement
            .newInstance("SELECT * FROM DEBS.ACCOUNT WHERE ID=?");
        statement.setParameter(1, this.id);

        final Query query = manager.createQuery(statement, Account.class);
        Assert.assertNotNull(query);
        Account account = (Account) query.execute().getSingleResult();

        Assert.assertNotNull(account);
        Assert.assertEquals(account.getName(), "CASH");
    }

    @Test
    public void resultsList() {
        final PersistenceManagerFactory factory = this.injector
            .getInstance(PersistenceManagerFactory.class);
        final QueryManager manager = factory.createQueryManager();
        Assert.assertNotNull(manager);

        SqlStatement statement = SqlStatement.newInstance("SELECT * FROM DEBS.ACCOUNT");

        final Query query = manager.createQuery(statement, Account.class);
        Assert.assertNotNull(query);
        List<Object> list = query.execute().getResultList();
        Assert.assertNotNull(list);
        Assert.assertFalse(list.isEmpty());
        Assert.assertTrue(list.size() == 2);
        Assert.assertTrue(Account.class.isInstance(list.get(0)));
    }

    public interface Account {

        Long getId();

        String getName();

        static Account newInstance(Map<String, Object> dataMap) {
            return new Account() {

                @Override
                public Long getId() {
                    return (Long) dataMap.get("ID");
                }

                @Override
                public String getName() {
                    return (String) dataMap.get("NAME");
                }
            };
        }
    }
}
