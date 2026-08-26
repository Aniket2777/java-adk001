package com.shrija.ai.config;

import com.google.adk.JsonBaseModel;
import com.google.adk.tools.mcp.McpToolset;
import com.google.adk.tools.mcp.StreamableHttpServerParameters;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class McpToolsetConfig {
  private static final List<String> EMPLOYEE_TOOLS =
      ImmutableList.of("getEmployeeProfile", "checkLeaveBalance", "applyLeave", "getLeaveHistory");
  private static final List<String> ATTENDANCE_TOOLS =
      ImmutableList.of("checkIn", "checkOut", "getAttendance");
  private static final List<String> PAYROLL_TOOLS =
      ImmutableList.of("getPayrollHistory", "getLatestSalary");
  private static final List<String> HR_TOOLS =
      ImmutableList.of("createEmployee", "transferEmployee", "terminateEmployee");

  @Bean
  public McpToolset employeeMcpToolset(ShrijaAiProperties properties) {
    return toolset(properties, EMPLOYEE_TOOLS);
  }

  @Bean
  public McpToolset attendanceMcpToolset(ShrijaAiProperties properties) {
    return toolset(properties, ATTENDANCE_TOOLS);
  }

  @Bean
  public McpToolset payrollMcpToolset(ShrijaAiProperties properties) {
    return toolset(properties, PAYROLL_TOOLS);
  }

  @Bean
  public McpToolset hrMcpToolset(ShrijaAiProperties properties) {
    return toolset(properties, HR_TOOLS);
  }

  private McpToolset toolset(ShrijaAiProperties properties, List<String> toolNames) {
    StreamableHttpServerParameters params =
        StreamableHttpServerParameters.builder().url(properties.mcpServerUrl()).build();
    return new McpToolset(params, JsonBaseModel.getMapper(), toolNames);
  }
}
