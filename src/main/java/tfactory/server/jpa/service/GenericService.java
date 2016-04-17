package tfactory.server.jpa.service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
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
    public void persistEntity(T entity) {
        doDML(entity, OPERATION.PERSIST);
    }

    /**
     * Updates and entity in the database.
     *
     * @param entity Entity to be updated.
     */
    public void updateEntity(T entity) {
        doDML(entity, OPERATION.UPDATE);
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
     * Removes and entity from the database based on its primary key.
     *
     * @param id PK of the entity to be removed.
     */
    public void removeEntity(Object id) {
        EntityManagerFactory factory = EMFProvider.getInstance().getEMF();
        EntityManager em = null;
        EntityTransaction trans = null;
        try {
            em = factory.createEntityManager();
            trans = em.getTransaction();
            trans.begin();
            T entity = em.getReference(type, id);
            em.remove(entity);
            trans.commit();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, String.format("Error in find operation for entity %s", type), ex);
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
     * Executes DML methods on the passed entity.
     *
     * @param entity Entity to work with.
     * @param op     {@link OPERATION} to be executed.
     */
    private void doDML(T entity, OPERATION op) {
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
                    em.remove(entity);
                    break;
                default:
                    return;
            }

            trans.commit();
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, String.format("Error executing %s on entity %s", op, entity.getClass()), ex);
            if (trans != null && trans.isActive()) {
                trans.rollback();
            }
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }
}
