package com.shrija.manager.tool;

import com.shrija.manager.a2a.LeaveAgentClient;
import com.shrija.manager.service.AuthorizationService;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LeaveApprovalCoordinationTool {

  private final LeaveAgentClient leaveAgentClient;
  private final AuthorizationService authorizationService;

  public LeaveApprovalCoordinationTool(
      LeaveAgentClient leaveAgentClient, AuthorizationService authorizationService) {
    this.leaveAgentClient = leaveAgentClient;
    this.authorizationService = authorizationService;
  }

  public String getPendingApprovals(String requesterEmployeeId, String requesterRole) {
    authorizationService.requireManagerPrivilege(requesterEmployeeId, requesterRole);
    return leaveAgentClient.getPendingApprovals(requesterEmployeeId);
  }

  public Map<String, Object> decideOnLeaveRequest(
      String requesterEmployeeId, String requesterRole, Long leaveRequestId, boolean approve) {
    authorizationService.requireManagerPrivilege(requesterEmployeeId, requesterRole);
    return leaveAgentClient.decideOnLeaveRequest(requesterEmployeeId, leaveRequestId, approve);
  }
}
