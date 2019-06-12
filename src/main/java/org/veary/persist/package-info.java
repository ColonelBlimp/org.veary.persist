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
 * <h2>Requirements:</h2>
 *
 * <ul><li>Google Guice</li></ul>
 *
 * <p>Entity interfaces which have a <b>static factory method</b> with the signature:
 * {@code static [interface] newInstance(Map<String, Object)} which creates an instance of the
 * class.
 *
 * <p>For example:
 *
 * <pre>
 *
 * public interface Person {
 *
 *     Long getId();
 *
 *     String getSurname();
 *
 *     String getForename();
 *
 *     static Person newInstance(Map<String, Object> dataMap) {
 *         // do all validation of the Map here
 *         return new Person() {
 *             Long getId() {
 *                 return (Long) dataMap.get("ID"); // Key is the name of the database field
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
 *
 * </pre>
 *
 * <pre>
 *
 * <h2>Usage:</h2>
 *
 * <pre>
 *
 * Injector injector = Guice.createInjector(
 *     ServiceLoader.load(com.google.inject.AbstractModule));
 *
 * QueryManager manager = QueryManager = injector.getInstance(QueryManager.class);
 *
 * Query query = manager.createQuery(
 *     SqlBuilder.newInstance("SELECT * FROM person WHERE id=?"),
 *     Person.class);
 * Person person = (Person) query
 *     .setParameter(1, Long.valueOf(10))
 *     .execute()
 *     .getSingleResult();
 * </pre>
 *
 */
package org.veary.persist;
