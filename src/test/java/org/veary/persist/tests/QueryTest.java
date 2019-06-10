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
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.veary.persist.Entity;
import org.veary.persist.Query;
import org.veary.persist.QueryManager;
import org.veary.persist.SqlBuilder;
import org.veary.persist.SqlStatement;
import org.veary.persist.TransactionManager;
import org.veary.persist.internal.GuicePersistModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

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
        this.injector = Guice.createInjector(
            new GuicePersistTestModule(),
            new GuicePersistModule());
    }

    @AfterClass
    public void teardown() {
        this.tomcatJndi.tearDown();
    }

    @Test
    public void createTables() {
        final TransactionManager txManager = this.injector.getInstance(TransactionManager.class);
        Assert.assertNotNull(txManager);
        txManager.begin();
        SqlBuilder createTableBuilder = SqlBuilder.newInstance(
            "CREATE TABLE IF NOT EXISTS debs.account(id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255))");
        SqlStatement createTable = SqlStatement.newInstance(createTableBuilder);
        txManager.persist(createTable);

        SqlBuilder insertAccount = SqlBuilder
            .newInstance("INSERT INTO debs.account(name) VALUES(?)");
        SqlStatement createAccountOne = SqlStatement.newInstance(insertAccount);
        createAccountOne.setParameter(1, "CASH");
        txManager.persist(createAccountOne);
        txManager.commit();

        this.id = Long.valueOf(txManager.getGeneratedIdList().get(0).intValue());
    }

    @Test
    public void singleResult() {
        final QueryManager manager = this.injector.getInstance(QueryManager.class);
        Assert.assertNotNull(manager);
        final Query query = manager.createQuery(
            SqlBuilder.newInstance("SELECT * FROM debs.account WHERE id=?"),
            Account.class);
        Assert.assertNotNull(query);
        Account account = (Account) query.setParameter(1, this.id).execute()
            .getSingleResult();

        Assert.assertNotNull(account);
        Assert.assertEquals(account.getName(), "CASH");
    }

    public interface Account extends Entity {

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
