package com.shrija.ai.config;

import com.google.adk.JsonBaseModel;
import com.google.adk.tools.mcp.McpToolset;
import com.google.adk.tools.mcp.SseServerParameters;
import com.google.adk.tools.mcp.StreamableHttpServerParameters;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class McpToolsetConfig {
    private static final List<String> EMPLOYEE_TOOLS = ImmutableList.of(
            "getEmployeeProfile", "getLeaveBalance", "applyForLeave", "getLeaveRequests");
    private static final List<String> ATTENDANCE_TOOLS = ImmutableList.of(
            "checkIn", "checkOut", "getAttendanceForRange");
    private static final List<String> PAYROLL_TOOLS = ImmutableList.of(
            "getPayrollHistory", "getLatestSalary");
    private static final List<String> HR_TOOLS = ImmutableList.of(
            "createEmployee", "recordTransfer", "recordExit");

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
        SseServerParameters params =
                SseServerParameters.builder().url(properties.mcpServerUrl()).build();
        return new McpToolset(params, JsonBaseModel.getMapper(), toolNames);
    }
}
