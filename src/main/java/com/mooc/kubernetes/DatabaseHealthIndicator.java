package com.mooc.kubernetes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseHealthIndicator implements  HealthIndicator{

    @Autowired
    private DataSource dataSource;

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) { // Check if the connection is valid
                return Health.up().build();
            } else {
                return Health.down().withDetail("error", "Database connection is not valid").build();
            }
        } catch (SQLException ex) {
            return Health.down(ex).build();
        }
    }

}
