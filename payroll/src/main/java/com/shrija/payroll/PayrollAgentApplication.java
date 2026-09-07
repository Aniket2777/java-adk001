package com.shrija.payroll;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point for the standalone Payroll Agent service.
 *
 * <p>This is its own Spring Boot process (separate from {@code adk-shrija-v2}, which hosts the
 * other department agents). It exposes one agent - Payroll - over its own REST endpoint, and does
 * all payroll data access exclusively through {@code mcp-server} via {@link
 * com.google.adk.tools.mcp.McpToolset}. No JDBC/JPA dependency exists in this module on purpose: if
 * you ever see a database driver added here, that's a sign someone bypassed MCP.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class PayrollAgentApplication {

  public static void main(String[] args) {
    SpringApplication.run(PayrollAgentApplication.class, args);
  }
}
