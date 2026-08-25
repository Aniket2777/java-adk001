package com.hrms.mcpserver.config;

import com.hrms.mcpserver.tools.AttendanceTools;
import com.hrms.mcpserver.tools.EmployeeTools;
import com.hrms.mcpserver.tools.HrTools;
import com.hrms.mcpserver.tools.LeaveTools;
import com.hrms.mcpserver.tools.PayrollTools;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires all 5 domain tool classes (Employee, Leave, Attendance, Payroll, HR) into ONE MCP server.
 * Spring AI's MCP server auto-config picks up any ToolCallbackProvider bean and exposes its tools
 * over the configured transport (SSE, per application.yml).
 */
@Configuration
public class McpToolConfig {

  @Bean
  public ToolCallbackProvider hrmsToolCallbackProvider(
      EmployeeTools employeeTools,
      LeaveTools leaveTools,
      AttendanceTools attendanceTools,
      PayrollTools payrollTools,
      HrTools hrTools) {
    return MethodToolCallbackProvider.builder()
        .toolObjects(employeeTools, leaveTools, attendanceTools, payrollTools, hrTools)
        .build();
  }

  @Bean
  public ToolCallback[] hrmsToolCallbacks(ToolCallbackProvider hrmsToolCallbackProvider) {
    return hrmsToolCallbackProvider.getToolCallbacks();
  }
}
