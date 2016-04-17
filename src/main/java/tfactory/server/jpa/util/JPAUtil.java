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

}
