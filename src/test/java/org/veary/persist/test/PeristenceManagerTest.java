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

        Account account = (Account) manager.createQuery(
            "CREATE TABLE IF NOT EXISTS debs.account(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE, name TEXT NOT NULL UNIQUE, description TEXT, type INTEGER NOT NULL)",
            Account.class).setParameter(1, "CASH").executeQuery().getSingleResult();
        Assert.assertNotNull(account);

        /**
         * PersistenceManager manager = new PersistenceManager() {
         *
         * @Override public Query createQuery(String sql, Class<? extends Entity>
         *           entityInterface) { if (!entityInterface.isInterface()) { throw new
         *           PersistenceException( "The entityInterface parameter is not an
         *           interface."); }
         *
         *           Method staticFactory; try { staticFactory =
         *           entityInterface.getDeclaredMethod("newInstance", Map.class); }
         *           catch (NoSuchMethodException | SecurityException e) { throw new
         *           PersistenceException(e); }
         *
         *           Map<String, Object> result = new HashMap<>(); result.put("name",
         *           "CASH"); result.put("id", Long.valueOf(1));
         *
         *           Object newInstance; try { newInstance =
         *           staticFactory.invoke(entityInterface, result); } catch
         *           (IllegalAccessException | IllegalArgumentException |
         *           InvocationTargetException e) { throw new PersistenceException(e); }
         *
         *           return new Query() {
         *
         *           final Map<String, Object> params = new HashMap<String, Object>();
         *
         * @Override public Object getSingleResult() { return newInstance; }
         *
         * @Override public List<? extends Entity> getResultList() { return
         *           Collections.emptyList(); }
         *
         * @Override public Query setParameter(int index, Object value) {
         *           this.params.put(String.valueOf(index), value); return this; }
         *
         * @Override public Query executeQuery() { return this; }
         *
         * @Override public int executeUpdate() { return 0; } }; } };
         */
//        Account acc = (Account) manager.createQuery("", Account.class).setParameter(1, "CASH")
//            .getSingleResult();
//        Assert.assertNotNull(acc);
//        Assert.assertEquals(acc.getName(), "CASH");
//        Assert.assertEquals(acc.getId(), Long.valueOf(1));
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
