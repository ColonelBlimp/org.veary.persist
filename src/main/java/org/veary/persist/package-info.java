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

/**
 * <h1>Introduction:</h1>
 *
 * <p>This library is a very basic wrapper around JDBC particularly providing an easier use of
 * transactions and automatic rollback functionality.
 *
 * <h2>Usage:</h2>
 *
 * <p>There are three manager available through the {@link PersistenceManagerFactory}
 *
 * <ul>
 *
 * <li>{@code QueryManager} which handles an SQL query which can return 0 or more results (e.g.
 * SELECT).</li>
 *
 * <li>{@code TransactionManager} which handles a SQL Data Manipulation Language (DML)
 * statement, such as INSERT, UPDATE or DELETE; or an SQL statement that returns nothing, such
 * as a DDL statement.</li>
 *
 * <li>{@code CallableManager} which handles SQL stored procedures.</li>
 *
 * </ul>
 *
 * <p>The above managers are accessed through the {@code PersistenceManagerFactory} which is
 * also annotated with JSR 330 {@code @Inject} and {@code @Singleton} for use with DI
 * containers. For example, with Google Guice you might have the following:
 *
 * <pre>
 * public class GuiceJndiModule extends AbstractModule {
 *
 *     &#64;Override
 *     protected void configure() {
 *         bind(Context.class).to(InitialContext.class);
 *         bind(DataSource.class).toProvider(
 *             JndiIntegration.fromJndi(DataSource.class, "java:/comp/env/jdbc/name"));
 *         bind(PersistenceManagerFactory.class);
 *     }
 * }
 * </pre>
 *
 * <h2>Entity Interfaces</h2>
 *
 * <p>Entity interfaces which use the library <b>must</b> have a <b>static factory method</b>
 * with the signature: {@code static [interface_name] newInstance(Map<String, Object)} which
 * creates an instance of the class.
 *
 * <p>For example:
 *
 * <pre>
 * public interface Person {
 *
 *     Long getId();
 *     String getSurname();
 *     String getForename();
 *
 *     static Person newInstance(Map&lt;String, Object&gt; dataMap) {
 *         // do all validation of the Map here
 *         return new Person() {
 *             Long getId() {
 *             Object obj = dataMap.get("ID");
 *                 return (Long) dataMap.get("ID"); // The key is the name of the database field
 *             }
 *
 *             String getSurname() {
 *                 return (String) dataMap.get("SURNAME");
 *             }
 *
 *             String getForename() {
 *             return (String) dataMap.get("FORNAME");
 *         }
 *     }
 * }
 * </pre>
 */

package org.veary.persist;
