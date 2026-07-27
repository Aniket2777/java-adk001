package com.shrija.mcpserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Entry point for the MCP DB server. Spring Boot's default component/ entity/repository scanning
 * only covers this class's own package tree ({@code com.shrija.mcpserver}) - since the JPA
 * entities, repositories, and {@code @Service} beans all live in the separate {@code
 * com.shrija.domain} package (the {@code adk-shrija-domain} module), all three scans are widened
 * explicitly below. Skipping this would fail silently at startup with "no qualifying bean" errors
 * for every domain service.
 */
@SpringBootApplication(scanBasePackages = {"com.shrija.mcpserver", "com.shrija.domain"})
@EntityScan("com.shrija.domain.model")
@EnableJpaRepositories("com.shrija.domain.repository")
public class McpShrijaServerApplication {

  public static void main(String[] args) {
    SpringApplication.run(McpShrijaServerApplication.class, args);
  }
}
