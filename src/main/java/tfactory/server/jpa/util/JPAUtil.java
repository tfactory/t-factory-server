/**
 * Copyright (C) 2015 Cesar Hernandez. (https://github.com/tfactory)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tfactory.server.jpa.util;

/**
 * Just an utility class with several constants and helper methods.
 * Not for instantiation
 * Created by aalopez on 4/17/16.
 */
public final class JPAUtil {

    /**
     * Avoids instantiation or inheritance
     */
    private JPAUtil() {
        throw new RuntimeException("Class not available for instantiation.");
    }

    /**
     * Persistence unit for tFactory server database
     */
    public static final String PERSISTENCE_DB_UNIT_NAME = "tFactoryServerUnit";

    /**
     * tFactory server database name or file
     */
    public static final String PERSISTENCE_DB_NAME = "tFactoryServerDB.sqlite";

    /**
     * tFactory server database name or file
     */
    public static final String PERSISTENCE_JDBC_STRING = "jdbc:sqlite:";

    /**
     * Gets the last throwable from the specified exception.
     *
     * @param e Exception to start from.
     * @return The last throwable from the specified exception.
     */
    public static Throwable getLastThrowable(Exception e) {
        Throwable t = null;
        for (t = e.getCause(); t.getCause() != null; t = t.getCause()) ;
        return t;
    }

}
