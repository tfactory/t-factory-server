package tfactory.server.jpa.service;

import tfactory.server.jpa.exception.TFactoryExceptionCodeEnum;
import tfactory.server.jpa.exception.TFactoryJPAException;
import tfactory.server.jpa.util.JPAUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Contains generic methods to persist, remove, update and find entities.
 * Created by aalopez on 4/16/16.
 */
public class GenericService<T> {

    private final static Logger LOGGER = Logger.getLogger(GenericService.class.getName());

    /**
     * Type of entity this class works with. Required since some JPA methods requires the type of entity.
     */
    private Class<T> type;

    /**
     * Enums different operations to be executed by this class
     */
    private enum OPERATION {
        PERSIST, UPDATE, REMOVE
    }

    /**
     * Creates a new instance of this class and sets the type param.
     *
     * @param type Type of entity this class is working with.
     */
    private GenericService(Class<T> type) {
        this.type = type;
    }

    /**
     * Creates an instance of this class using the specified type passed as param.
     *
     * @param type Type of entity this class works with.
     * @param <U>  Generic type for the new instance.
     * @return Instance of this class which its generic type is the one passed as param.
     */
    public static <U> GenericService<U> of(Class<U> type) {

        return new GenericService<>(type);
    }

    /**
     * Persists the entity to the database.
     *
     * @param entity Entity to be persisted.
     */
    public void persistEntity(T entity) throws TFactoryJPAException {
        doDML(entity, OPERATION.PERSIST);
    }

    /**
     * Updates and entity in the database.
     *
     * @param entity Entity to be updated.
     */
    public void updateEntity(T entity) throws TFactoryJPAException {
        doDML(entity, OPERATION.UPDATE);
    }

    /**
     * Removes an entity from the database based on its primary key.
     *
     * @param entity Entity to be removed.
     */
    public void removeEntity(T entity) throws TFactoryJPAException {
        doDML(entity, OPERATION.REMOVE);
    }

    /**
     * Executes DML methods on the passed entity.
     *
     * @param entity Entity to work with.
     * @param op     {@link OPERATION} to be executed.
     */
    private void doDML(T entity, OPERATION op) throws TFactoryJPAException {
        EntityManagerFactory factory = EMFProvider.getInstance().getEMF();
        EntityManager em = null;
        EntityTransaction trans = null;
        try {
            em = factory.createEntityManager();
            trans = em.getTransaction();
            trans.begin();

            switch (op) {
                case PERSIST:
                    em.persist(entity);
                    break;
                case UPDATE:
                    em.merge(entity);
                    break;
                case REMOVE:
                    em.remove(em.getReference(type, getEntityPK(entity)));
                    break;
                default:
                    break;
            }

            trans.commit();
        } catch (PersistenceException ex) {
            LOGGER.log(Level.SEVERE, String.format("Error executing %s on entity %s", op, entity.getClass()), ex);

            //get the last throwable of this exception and check if it is a SQLException
            Throwable t = JPAUtil.getLastThrowable(ex);
            if (t instanceof SQLException) {
                SQLException sql = (SQLException) t;
                //check whether the exception is about constraint violation
                if ("23000".equals(sql.getSQLState()) || sql.getMessage().toUpperCase().contains("CONSTRAINT")) {
                    LOGGER.log(Level.WARNING, "Constraint Violation");
                    throw new TFactoryJPAException(TFactoryExceptionCodeEnum.CONSTRAINT_VIOLATION, sql);
                }
            }

            if (trans != null && trans.isActive()) {
                trans.rollback();
            }
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    /**
     * Gets the primary key for the specified entity.
     *
     * @param entity to be queried.
     * @return PK of the specified entity.
     */
    private Object getEntityPK(T entity) {
        return EMFProvider.getInstance().getEMF().getPersistenceUnitUtil().getIdentifier(entity);
    }

    /**
     * Finds an entity based on its primary key.
     *
     * @param id PK
     * @return Entity identified by the passed param, if found.
     */
    public Optional<T> findByPk(Object id) {
        EntityManagerFactory factory = EMFProvider.getInstance().getEMF();
        EntityManager em = null;
        Optional<T> result = null;
        try {
            em = factory.createEntityManager();
            result = Optional.of(em.find(type, id));
        } catch (Exception ex) {
            result = Optional.empty();
            LOGGER.log(Level.SEVERE, String.format("Error in find operation for entity %s", type), ex);
        } finally {
            if (em != null) {
                em.close();
            }
        }

        return result;
    }

    /**
     * Gets all entities from database.
     * For this to work, entities should be marked with @Entity annotation and the name of the entity must match the name of the class.
     *
     * @return {@link List} of all entities T registered in database.
     */
    @SuppressWarnings("unchecked")
    public List<T> findAll() {
        EntityManagerFactory factory = EMFProvider.getInstance().getEMF();
        EntityManager em = null;

        try {
            em = factory.createEntityManager();

            //return all entities using JQPL
            StringBuilder builder = new StringBuilder("SELECT t FROM ").append(type.getSimpleName()).append(" t");
            return em.createQuery(builder.toString()).getResultList();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, String.format("Error in find all operation for entity %s", type), ex);
            return Collections.EMPTY_LIST;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
}
