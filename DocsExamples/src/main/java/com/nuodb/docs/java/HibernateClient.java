package com.nuodb.docs.java;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Examples of using NuoDB's Hibernate support in a pure Hibernate application
 * and in a JPA application.
 * 
 * @author Paul Chapman
 */
public class HibernateClient {

    Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Run using Hibernate Session API. This is no longer preferred. JPA is
     * recommended instead.
     */
    public void runUsingSession() {
        Configuration configuration = new Configuration();
        configuration.configure();

        // The factory is normally a singleton that exists for as long as the
        // application is running.
        SessionFactory factory = configuration.buildSessionFactory();

        try (Session session = factory.openSession();) {

            String id = "aaltoan01";
            Player p1 = session.find(Player.class, id);

            System.out.println("Found: " + p1.firstName + ' ' + p1.lastName);
            System.out.println("       height: " + p1.height + "\" weight: " + p1.weight + "lb");
            System.out.println("       active: " + p1.firstNHL + '-' + p1.lastNHL);
            System.out.println(String.format("       born: %4d-%02d-%02d in %s, %s", p1.birthYear, p1.birthMon,
                    p1.birthDay, p1.birthCity, p1.birthCountry));
        }

        // Only close the factory when the application is finished
        factory.close();
    }

    /**
     * Run using JPA with Hibernate as the persistence provider.
     */
    private void runUsingJpa() {
        EntityManager entityManager = null;

        try {
            EntityManagerFactory factory = Persistence.createEntityManagerFactory("Hockey");
            entityManager = factory.createEntityManager();

            // Read the existing entries and write to console
            String id = "aaltoan01";
            TypedQuery<Player> q = entityManager.createQuery("SELECT p FROM Player p WHERE p.id = :id", Player.class);
            q.setParameter("id", id);
            Player p1 = q.getSingleResult();

            System.out.println("Found: " + p1.firstName + ' ' + p1.lastName);
            System.out.println("       height: " + p1.height + "\" weight: " + p1.weight + "lb");
            System.out.println("       active: " + p1.firstNHL + '-' + p1.lastNHL);
            System.out.println(String.format("       born: %4d-%02d-%02d in %s, %s", p1.birthYear, p1.birthMon,
                    p1.birthDay, p1.birthCity, p1.birthCountry));
        } finally {
            if (entityManager != null)
                entityManager.close();
        }
        
    }

    /**
     * Run the examples. Each should output:
     * 
     * <pre>
     * Found: Antti Aalto
     *        height: 73" weight: 210lb
     *        active: 1997-2000
     *        born: 1975-03-04 in Lappeenranta, Finland
     * </pre>
     * 
     * @param args
     */
    public static void main(String[] args) {
        new HibernateClient().runUsingSession();
        new HibernateClient().runUsingJpa();
    }
}
