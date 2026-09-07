package com.shrija.manager.tool;

import com.shrija.manager.mcp.ManagerMcpClient;
import com.shrija.manager.service.AuthorizationService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Read-only team roster tool. The MCP server does not (yet) expose a dedicated
 * "get direct reports" tool, so this filters the confirmed listEmployees() result by
 * managerEmployeeId client-side rather than inventing any data. (A dedicated
 * getDirectReports(managerEmployeeId) MCP tool would be a natural future enhancement.)
 */
@Component
public class TeamRosterTool {

  private final ManagerMcpClient mcpClient;
  private final AuthorizationService authorizationService;

  public TeamRosterTool(ManagerMcpClient mcpClient, AuthorizationService authorizationService) {
    this.mcpClient = mcpClient;
    this.authorizationService = authorizationService;
  }

  public List<Map<String, Object>> getTeamMembers(
      String requesterEmployeeId, String requesterRole) {
    authorizationService.requireManagerPrivilege(requesterEmployeeId, requesterRole);

    List<Map<String, Object>> allEmployees = mcpClient.callList("listEmployees", Map.of());

    return allEmployees.stream()
        .filter(
            employee ->
                requesterEmployeeId.equals(String.valueOf(employee.get("managerEmployeeId"))))
        .collect(Collectors.toList());
  }

  public Map<String, Object> getTeamMemberProfile(
      String requesterEmployeeId, String requesterRole, Long employeeId) {
    authorizationService.requireManagerPrivilege(requesterEmployeeId, requesterRole);

    Map<String, Object> profile =
        mcpClient.call("getEmployeeProfile", Map.of("employeeId", employeeId));

    if (!requesterEmployeeId.equals(String.valueOf(profile.get("managerEmployeeId")))) {
      throw new SecurityException("Employee " + employeeId + " is not on this manager's team.");
    }
    return profile;
  }
}
