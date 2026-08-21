package com.shrija.ai.a2a;

import com.google.adk.agents.BaseAgent;
import com.shrija.ai.config.ShrijaAiProperties;
import org.springframework.stereotype.Component;

@Component
public class EmployeeAgentClient {
  private final ShrijaAiProperties properties;
  private final A2AAgentClientSupport support;

  public EmployeeAgentClient(ShrijaAiProperties properties, A2AAgentClientSupport support) {
    this.properties = properties;
    this.support = support;
  }

  public BaseAgent connect() {
    return support.connect(properties.employeeAgentUrl(), "employee-agent", "Employee Agent");
  }
}
