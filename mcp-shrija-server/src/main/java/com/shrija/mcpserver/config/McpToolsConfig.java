package com.shrija.mcpserver.config;

import com.shrija.mcpserver.tools.DocumentMcpTools;
import com.shrija.mcpserver.tools.HrDirectoryMcpTools;
import com.shrija.mcpserver.tools.LeaveMcpTools;
import com.shrija.mcpserver.tools.LifecycleMcpTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registers every {@code @Tool}-annotated method on the four tool classes as an MCP tool. Spring
 * AI's MCP server autoconfiguration (from {@code spring-ai-starter-mcp-server-webmvc}) picks up
 * {@link ToolCallbackProvider} beans automatically and exposes each tool over the configured
 * transport - nothing else in this module wires the MCP protocol itself, which is the point of
 * using the starter instead of hand-rolling transport/session handling.
 */
@Configuration
public class McpToolsConfig {

  @Bean
  public ToolCallbackProvider shrijaToolCallbackProvider(
      HrDirectoryMcpTools hrDirectoryMcpTools,
      LeaveMcpTools leaveMcpTools,
      DocumentMcpTools documentMcpTools,
      LifecycleMcpTools lifecycleMcpTools) {
    return MethodToolCallbackProvider.builder()
        .toolObjects(hrDirectoryMcpTools, leaveMcpTools, documentMcpTools, lifecycleMcpTools)
        .build();
  }
}
