package com.shrija.manager.a2a;

import com.shrija.manager.config.ManagerAiProperties;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class HrAgentClient {

  private final ManagerAiProperties properties;
  private final A2AAgentClientSupport support;

  public HrAgentClient(ManagerAiProperties properties, A2AAgentClientSupport support) {
    this.properties = properties;
    this.support = support;
  }

  /**
   * Escalates a team matter that is outside the Manager Agent's own authority - e.g. a policy
   * exception, a headcount/backfill request, or a performance concern that HR must own.
   */
  public Map<String, Object> escalateToHr(
      String managerEmployeeId, String subject, String details) {
    String response =
        support.call(
            properties.hrAgentUrl(),
            "Manager "
                + managerEmployeeId
                + " is escalating a team matter to HR. Subject: "
                + subject
                + ". Details: "
                + details
                + ". Log this escalation and confirm receipt.");
    return Map.of(
        "escalated", true, "subject", subject, "hrAgentResponse", response);
  }
}
