package com.shrija.ai.config;

import com.google.adk.JsonBaseModel;
import com.google.adk.tools.mcp.McpToolset;
import com.google.adk.tools.mcp.StreamableHttpServerParameters;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Two {@link McpToolset} beans, both pointed at the same {@code mcp-shrija-server} endpoint but
 * each filtered (via the toolset's {@code toolNames} allowlist constructor) to only the tools that
 * agent is allowed to call.
 *
 * <p>This filtering exists for a real reason, not just tidiness: the MCP server exposes every
 * HR/leave/document tool from one endpoint. Without a client-side allowlist, giving the Employee
 * Agent an unfiltered toolset would hand it {@code approveLeaveRequest}/{@code
 * rejectLeaveRequest}/{@code markDocumentReady} etc. - exactly the self-approval capability the
 * earlier split between {@code EmployeeSelfServiceService} and {@code LeaveApprovalService} was
 * built to prevent. The boundary that used to live in "which Java class has a reference to which
 * service" now lives here instead.
 */
@Configuration
public class McpToolsetConfig {

  private static final List<String> HR_TOOL_NAMES =
      ImmutableList.of(
          "getEmployeeByCode",
          "listEmployeesByDepartment",
          "addEmployee",
          "deleteEmployee",
          "transferEmployee",
          "listPendingLeaveRequests",
          "approveLeaveRequest",
          "rejectLeaveRequest",
          "listPendingDocumentRequests",
          "markDocumentReady",
          "markDocumentDelivered");

  private static final List<String> EMPLOYEE_TOOL_NAMES =
      ImmutableList.of(
          "checkLeaveBalance",
          "applyForLeave",
          "checkOnboardingOffboardingStatus",
          "requestDocument",
          "checkDocumentRequestStatus");

  @Bean
  public McpToolset hrMcpToolset(ShrijaAiProperties properties) {
    return new McpToolset(connectionParams(properties), JsonBaseModel.getMapper(), HR_TOOL_NAMES);
  }

  @Bean
  public McpToolset employeeMcpToolset(ShrijaAiProperties properties) {
    return new McpToolset(
        connectionParams(properties), JsonBaseModel.getMapper(), EMPLOYEE_TOOL_NAMES);
  }

  private StreamableHttpServerParameters connectionParams(ShrijaAiProperties properties) {
    return StreamableHttpServerParameters.builder().url(properties.mcpServerUrl()).build();
  }
}
