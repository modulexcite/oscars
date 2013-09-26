package net.es.nsi.cli.db;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class PersistenceHolder {
    private static final EntityManagerFactory emf;
    private static final ThreadLocal<EntityManager> threadLocal;

    static {
        emf = Persistence.createEntityManagerFactory("net.es.nsi.cli");
        threadLocal = new ThreadLocal<EntityManager>();
    }

    private PersistenceHolder() {
    }

    public static EntityManager getEntityManager() {
        EntityManager em = threadLocal.get();

        if (em == null) {
            em = emf.createEntityManager();
            // set your flush mode here
            threadLocal.set(em);
        }
        return em;
    }

    public static void closeEntityManager() {
        EntityManager em = threadLocal.get();
        if (em != null) {
            em.close();
            threadLocal.set(null);
        }
    }

    public static void closeEntityManagerFactory() {
        emf.close();
    }

    public static void beginTransaction() {
        getEntityManager().getTransaction().begin();
    }

    public static void rollback() {
        getEntityManager().getTransaction().rollback();
    }

    public static void commit() {
        getEntityManager().getTransaction().commit();
    }
}
