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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.veary.persist.PersistenceManager;
import org.veary.persist.QueryBuilder;
import org.veary.persist.entity.Entity;
import org.veary.persist.exceptions.NoResultException;
import org.veary.persist.exceptions.PersistenceException;

import com.google.inject.Guice;
import com.google.inject.Injector;

import hthurow.tomcatjndi.TomcatJNDI;

public class QueryTest {

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

	@Test
	public void createTable() {
		PersistenceManager manager = this.injector.getInstance(PersistenceManager.class);
		Assert.assertNotNull(manager);

		QueryBuilder builder = QueryBuilder
			.newInstance(
				"CREATE TABLE IF NOT EXISTS debs.account(id INT PRIMARY KEY AUTO_INCREMENT, name VARCHAR(255) NOT NULL)");
		Assert.assertEquals(manager.createQuery(builder).executeUpdate(), Long.valueOf(0));
	}

	@Test(dependsOnMethods = { "createTable" })
	public void createAndReadAccount() {
		PersistenceManager manager = this.injector.getInstance(PersistenceManager.class);
		Assert.assertNotNull(manager);

		QueryBuilder insertSql = QueryBuilder
			.newInstance("INSERT INTO debs.account(name) VALUES(?)");

		Long result = manager
			.createQuery(insertSql, Account.class)
			.setParameter(1, "CASH")
			.executeUpdate();
		Assert.assertNotNull(result);
		Assert.assertEquals(result, Long.valueOf(1));

		QueryBuilder selectSql = QueryBuilder
			.newInstance("SELECT * FROM debs.account WHERE id=?");
		Account account = (Account) manager
			.createQuery(selectSql, Account.class)
			.setParameter(1, result)
			.executeQuery()
			.getSingleResult();

		Assert.assertNotNull(account);
		Assert.assertEquals(account.getName(), "CASH");
	}

	@Test(dependsOnMethods = { "createTable" }, expectedExceptions = NoResultException.class)
	public void noResultExeption() {
		PersistenceManager manager = this.injector.getInstance(PersistenceManager.class);
		Assert.assertNotNull(manager);

		QueryBuilder selectSql = QueryBuilder
			.newInstance("SELECT * FROM debs.account WHERE id=?");

		manager
			.createQuery(selectSql, Account.class)
			.setParameter(1, Long.valueOf(2))
			.executeQuery()
			.getSingleResult();
	}

	@Test(
		expectedExceptions = PersistenceException.class,
		expectedExceptionsMessageRegExp = "Query result not set. Call Query.executeQuery\\(\\) before calling Query.getSingleResult\\(\\).")
	public void methodOrderException() {
		PersistenceManager manager = this.injector.getInstance(PersistenceManager.class);
		Assert.assertNotNull(manager);

		QueryBuilder selectSql = QueryBuilder
			.newInstance("SELECT * FROM debs.account WHERE id=?");

		manager
			.createQuery(selectSql, Account.class)
			.setParameter(1, Long.valueOf(2))
			.getSingleResult();
	}

	@Test(
		expectedExceptions = IllegalStateException.class,
		expectedExceptionsMessageRegExp = "You cannot call Query.executeQuery\\(\\) on this query. It is the incorrect query type.")
	public void incorrectQueryTypeInsert() {
		PersistenceManager manager = this.injector.getInstance(PersistenceManager.class);
		Assert.assertNotNull(manager);

		QueryBuilder insertSql = QueryBuilder
			.newInstance("INSERT INTO debs.account(name) VALUES(?)");

		manager.createQuery(insertSql, Account.class).setParameter(1, "CASH").executeQuery();
	}

	@Test(
		expectedExceptions = IllegalStateException.class,
		expectedExceptionsMessageRegExp = "You cannot call Query.executeUpdate\\(\\) on this query. It is the incorrect query type.")
	public void incorrectQueryTypeSelect() {
		PersistenceManager manager = this.injector.getInstance(PersistenceManager.class);
		Assert.assertNotNull(manager);

		QueryBuilder selectSql = QueryBuilder.newInstance("SELECT * FROM debs.account");

		manager.createQuery(selectSql, Account.class).executeUpdate();
	}

	@Test(
		expectedExceptions = IllegalArgumentException.class,
		expectedExceptionsMessageRegExp = "String SQL statement must be non-empty.")
	public void emptySqlString() {
		PersistenceManager manager = this.injector.getInstance(PersistenceManager.class);
		Assert.assertNotNull(manager);

		QueryBuilder sql = QueryBuilder.newInstance("");

		manager.createQuery(sql, Account.class).executeUpdate();
	}

	@Test(
		expectedExceptions = IllegalArgumentException.class,
		expectedExceptionsMessageRegExp = "Query.setParameter index starts a 1.")
	public void illegalParameterIndexException() {
		PersistenceManager manager = this.injector.getInstance(PersistenceManager.class);
		Assert.assertNotNull(manager);

		QueryBuilder insertSql = QueryBuilder
			.newInstance("INSERT INTO debs.account(name) VALUES(?)");

		manager.createQuery(insertSql, Account.class).setParameter(0, "CASH").executeUpdate();
	}

	@Test(
		expectedExceptions = NullPointerException.class,
		expectedExceptionsMessageRegExp = "QueryBuilder parameter is null.")
	public void illegalParameterNullStringException() {
		PersistenceManager manager = this.injector.getInstance(PersistenceManager.class);
		Assert.assertNotNull(manager);

		manager.createQuery(null, null).executeUpdate();
	}

	@Test(
		expectedExceptions = NullPointerException.class,
		expectedExceptionsMessageRegExp = "Entity interface parameter is null.")
	public void illegalParameterNullIFaceException() {
		PersistenceManager manager = this.injector.getInstance(PersistenceManager.class);
		Assert.assertNotNull(manager);

		QueryBuilder selectSql = QueryBuilder.newInstance("SELECT * FROM account");

		manager.createQuery(selectSql, null).executeQuery();
	}

	public interface Account extends Entity {

		String getName();

		static Account newInstance(Map<String, Object> dataMap) {
			return new Account() {

				@Override
				public String getName() {
					return (String) dataMap.get("NAME");
				}

				@Override
				public Long getId() {
					return (Long) dataMap.get("ID");
				}
			};
		}
	}
}
