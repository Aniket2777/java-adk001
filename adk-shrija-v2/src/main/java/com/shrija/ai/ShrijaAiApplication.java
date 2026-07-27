package com.shrija.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point for the Shrija AI enterprise assistant.
 *
 * <p>Shrija AI is a multi-agent system built on Google ADK where a central {@code ManagerAgent}
 * orchestrates department-specific agents (HR, Payroll, Budget, Settlement, Report, Notification,
 * Employee). See {@code agent} package for agent factories and {@code service.ConversationService}
 * for the orchestration entry point used by the REST layer.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class ShrijaAiApplication {

  public static void main(String[] args) {
    SpringApplication.run(ShrijaAiApplication.class, args);
  }
}
