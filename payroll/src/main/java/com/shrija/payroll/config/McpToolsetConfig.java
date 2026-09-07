package com.shrija.payroll.config;

import com.google.adk.JsonBaseModel;
import com.google.adk.tools.mcp.McpToolset;
import com.google.adk.tools.mcp.SseServerParameters;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Connects this service to the shared {@code mcp-server} over Streamable HTTP and restricts the
 * agent to only the payroll tool names the server actually exposes (see {@code PayrollTools} in
 * {@code mcp-server}). No direct database access from this module - every payroll operation goes
 * through this toolset.
 */
@Configuration
public class McpToolsetConfig {

  private static final List<String> PAYROLL_TOOLS =
      ImmutableList.of("generateSalarySlip", "markSalarySlipAsPaid", "getSalarySlip");

  @Bean
  public McpToolset payrollMcpToolset(PayrollAgentProperties properties) {
    SseServerParameters params =
        SseServerParameters.builder().url(properties.mcpServerUrl()).build();
    return new McpToolset(params, JsonBaseModel.getMapper(), PAYROLL_TOOLS);
  }
}
