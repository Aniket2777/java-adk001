package com.shrija.manager.tool;

import com.shrija.manager.a2a.HrAgentClient;
import com.shrija.manager.service.AuthorizationService;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class HrEscalationTool {

  private final HrAgentClient hrAgentClient;
  private final AuthorizationService authorizationService;

  public HrEscalationTool(HrAgentClient hrAgentClient, AuthorizationService authorizationService) {
    this.hrAgentClient = hrAgentClient;
    this.authorizationService = authorizationService;
  }

  public Map<String, Object> escalateToHr(
      String requesterEmployeeId, String requesterRole, String subject, String details) {
    authorizationService.requireManagerPrivilege(requesterEmployeeId, requesterRole);
    return hrAgentClient.escalateToHr(requesterEmployeeId, subject, details);
  }
}
