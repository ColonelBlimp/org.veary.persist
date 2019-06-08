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

package org.veary.persist.test;

import java.io.File;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.veary.persist.PersistenceManager;
import org.veary.persist.entity.Entity;

import com.google.inject.Guice;
import com.google.inject.Injector;

import hthurow.tomcatjndi.TomcatJNDI;

public class PeristenceManagerTest {

    private TomcatJNDI tomcatJndi;
    private Injector injector;

    @BeforeTest
    public void setUp() {
        final File contextXml = new File("src/test/resources/context.xml");
        this.tomcatJndi = new TomcatJNDI();
        this.tomcatJndi.processContextXml(contextXml);
        this.tomcatJndi.start();
        this.injector = Guice.createInjector(new GuicePersistTestModule());
    }

    @Test
    public void createTable() {
        PersistenceManager manager = this.injector.getInstance(PersistenceManager.class);
        Assert.assertNotNull(manager);

        Assert.assertEquals(manager.createQuery(
            "CREATE TABLE IF NOT EXISTS debs.account(id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255) NOT NULL)")
            .executeUpdate(), Long.valueOf(0));
    }

    @Test(dependsOnMethods = { "createTable" })
    public void createAccountRow() {
        PersistenceManager manager = this.injector.getInstance(PersistenceManager.class);
        Assert.assertNotNull(manager);

        Long result = manager
            .createQuery("INSERT INTO debs.account(name) VALUES(?)", Account.class)
            .setParameter(1, "CASH")
            .executeUpdate();
    }

    public interface Account extends Entity {

        String getName();

        static Account newInstance(Map<String, Object> dataMap) {
            return new Account() {

                @Override
                public String getName() {
                    return (String) dataMap.get("name");
                }

                @Override
                public Long getId() {
                    return (Long) dataMap.get("id");
                }
            };
        }
    }
}
