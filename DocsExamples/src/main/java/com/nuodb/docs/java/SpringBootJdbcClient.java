package com.nuodb.docs.java;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.admin.SpringApplicationAdminJmxAutoConfiguration;
import org.springframework.boot.autoconfigure.jmx.JmxAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;

/**
 * JDBC DataSource using Spring Boot.
 * <p>
 * Expected output is:
 * 
 * <pre>
 * Data source is a class com.zaxxer.hikari.HikariDataSource
 * Found: Antti Aalto
 * </pre>
 * 
 * @author Paul Chapman
 */
@EnableTransactionManagement(proxyTargetClass = true)
@SpringBootApplication(scanBasePackages = "no-such-package", //
        exclude = { JmxAutoConfiguration.class, SpringApplicationAdminJmxAutoConfiguration.class })
public class SpringBootJdbcClient {

    public static void main(String[] args) {
        // Run Spring Boot, using this class as the starting point for configuration.
        SpringApplication.run(SpringBootJdbcClient.class, args);
    }

    @Bean
    public DataSource dataSource() throws IOException {
        // Using a Java properties file
        Properties properties = new Properties();
        properties.load(new FileInputStream("nuodb.properties"));

        return new com.nuodb.jdbc.DataSource(properties);
    }

    /**
     * Spring Boot recommended way to run code at startup. As this class is marked
     * as an {@code @Component}, Spring will automatically create an instance. As it
     * implements {@code CommandLineRunner}, spring Boot will automatically invoke
     * its run method after all configuration is complete.
     */
    @Component
    public class ClientRunner implements CommandLineRunner {

        // Spring Boot will automatically create the data source and set this
        // data-member (even though it is private).
        @Autowired
        private DataSource dataSource;

        @Override
        @Transactional
        public void run(String... args) throws Exception {
            System.out.println("Data source is a " + dataSource.getClass());

            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            Map<String, Object> result = jdbcTemplate
                    .queryForMap("SELECT firstname, lastname FROM Players WHERE playerid=?", "aaltoan01");

            // The map is an instance of org.springframework.util.LinkedCaseInsensitiveMap
            // The keys are case insensitive.
            System.out.println("Found: " + result.get("FIRSTNAME") + ' ' + result.get("lastname"));

            // This is not a long-running application but Spring Boot doesn't know that,
            // quit now
            System.exit(0);
        }

    }
}
