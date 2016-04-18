package tfactory.server.jpa.service;

import tfactory.server.jpa.util.JPAUtil;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Singleton that provides access to the entity manager factory which is an expensive class to create.
 * Created by aalopez on 4/17/16.
 */
public class EMFProvider {

    private final static Logger LOGGER = Logger.getLogger(EMFProvider.class.getName());

    /**
     * Unique instance of this class.
     */
    private static EMFProvider instance;

    /**
     * Unique Entity Manager Factory created for the current app.
     */
    private EntityManagerFactory emf;

    private EMFProvider() {
    }

    /**
     * @return Unique instance of this class.
     */
    public static EMFProvider getInstance() {
        if (instance == null) {
            instance = new EMFProvider();
        }

        return instance;
    }

    /**
     * Gets the unique entity manager factory.
     * If it has not been created yet, it will created before returning.
     *
     * @return Unique instance of entity manager factory for the application.
     */
    public EntityManagerFactory getEMF() {
        //if emf is not created yet, then created
        if (emf == null) {
            //we need the path to the META-INF directory of the application
            URL url = this.getClass().getResource("/META-INF/");

            //create the jdbc url for the database
            StringBuilder jdbcUrlBuilder = new StringBuilder(JPAUtil.PERSISTENCE_JDBC_STRING)
                    .append(url.toString())
                    .append(JPAUtil.PERSISTENCE_DB_NAME);

            //set the database URL based on the path to the META-INF app of the application
            Map<String, String> properties = new HashMap<>();
            properties.put("javax.persistence.jdbc.url", jdbcUrlBuilder.toString());

            // Setup the entity manager factory
            emf = Persistence.createEntityManagerFactory(JPAUtil.PERSISTENCE_DB_UNIT_NAME, properties);
        }

        return emf;
    }

    /**
     * Closes the unique entity manager factory for the app.
     * Sets the internal variable to null so it can be created again later.
     */
    public void closeEMF() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
    }
}
