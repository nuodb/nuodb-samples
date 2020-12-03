package com.nuodb.docs.java;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

/**
 * JPA and Hibernate using Spring Boot.
 * 
 * @author Paul Chapman
 */
@EntityScan("com.nuodb.docs")
@EnableTransactionManagement(proxyTargetClass = true)
@SpringBootApplication(scanBasePackages = "no-such-package", //
        exclude = { JmxAutoConfiguration.class, SpringApplicationAdminJmxAutoConfiguration.class })
public class SpringBootJpaClient {

    public static void main(String[] args) {
        // Run Spring Boot, using this class as the starting point for configuration.
        SpringApplication.run(SpringBootJpaClient.class, args);
    }

    /**
     * Spring Boot recommended way to run code at startup. As this class is marked
     * as an {@code @Component}, Spring will automatically create an instance. As it
     * implements {@code CommandLineRunner}, spring Boot will automatically invoke
     * its run method after all configuration is complete.
     */
    @Component
    public class ClientRunner implements CommandLineRunner {

        // Spring will automatically create a context and set this data-member (even
        // though it is private).
        @PersistenceContext
        private EntityManager entityManager;

        @Override
        @Transactional
        public void run(String... args) throws Exception {
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

            // This is not a long-running application but Spring Boot doesn't know that,
            // quit now
            System.exit(0);
        }

    }
}
