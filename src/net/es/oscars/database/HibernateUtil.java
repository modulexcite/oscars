package net.es.oscars.database;

import java.util.*;

import org.hibernate.*;
import org.hibernate.cfg.*;

import net.es.oscars.PropHandler;

/**
 * HibernateUtil is adapted from the tutorial in the Hibernate 3.2
 * distribution.
 * It maintains a hash map of session factories with one entry for each item
 * in the list of db names given to initSessionFactories.
 *
 * @author (besides Hibernate developers) dwrobertson@lbl.gov, mrthompson@lbl.gov
 */
public class HibernateUtil {

    private static final Map<String, SessionFactory> sessionFactories =
        new HashMap<String,SessionFactory>();

    /**
     * Called from OSCARSSkeleton and test setup. When running in axis, we need
     *     to set the classLoader in order for it to find the .xml files.
     *
     * @param CL classloader for the calling class
     * @param dbnames list of db names to build session factories for
     */
    public static void initSessionFactories(List<String> dbnames) {
        try {
            PropHandler propHandler = new PropHandler("oscars.properties");
            if (propHandler == null) {
                throw new ExceptionInInitializerError("Could not find properties file");
            }
            Properties props = propHandler.getPropertyGroup("hibernate", false);
            Configuration cfg = new Configuration();
            cfg.setProperties(props);

            for (String dbname: dbnames) {
                if (sessionFactories.get(dbname) == null) {
                    SessionFactory sessionFactory = cfg.configure(dbname + ".cfg.xml").buildSessionFactory();
                    putSessionFactory(dbname, sessionFactory);
                }
            }
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex.getMessage());
        }
    }

    public static void destroySessionFactories() {
        Iterator<String> dbNamesIt = sessionFactories.keySet().iterator();
        while (dbNamesIt.hasNext()) {
            String dbName = dbNamesIt.next();
            HibernateUtil.closeSessionFactory(dbName);
        }
    }

    public static void closeSessionFactory(String factoryName) {
        SessionFactory sessionFactory = sessionFactories.get(factoryName);
        if (sessionFactory != null) {
            Session session = sessionFactory.getCurrentSession();
            if (session != null) {
                session.close();
            }
            sessionFactory.close();
        }
    }

    public static SessionFactory getSessionFactory(String factoryName) {
        return sessionFactories.get(factoryName);
    }

    public static void putSessionFactory (String name, SessionFactory SF) {
        sessionFactories.put(name, SF);
    }





}
