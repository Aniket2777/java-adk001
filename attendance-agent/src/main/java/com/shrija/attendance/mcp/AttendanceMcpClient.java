package com.shrija.attendance.mcp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.adk.JsonBaseModel;
import com.google.adk.tools.mcp.McpSessionManager;
import com.google.adk.tools.mcp.SseServerParameters;
import com.google.common.collect.ImmutableMap;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AttendanceMcpClient implements AutoCloseable {

  private static final Logger log = LoggerFactory.getLogger(AttendanceMcpClient.class);

  private final McpSessionManager sessionManager;
  private final ObjectMapper objectMapper;
  private volatile McpSyncClient client;

  public AttendanceMcpClient(com.shrija.attendance.config.AttendanceAiProperties properties) {
    this.sessionManager =
        new McpSessionManager(SseServerParameters.builder().url(properties.mcpServerUrl()).build());
    this.objectMapper = JsonBaseModel.getMapper();
  }

  public synchronized void connect() {
    if (client == null) {
      client = sessionManager.createSession();
    }
  }

  public Map<String, Object> call(String toolName, Map<String, Object> arguments) {
    RuntimeException last = null;
    for (int attempt = 1; attempt <= 3; attempt++) {
      try {
        connect();
        CallToolResult result =
            client.callTool(new CallToolRequest(toolName, ImmutableMap.copyOf(arguments)));
        return decode(result);
      } catch (RuntimeException ex) {
        last = ex;
        log.warn(
            "MCP attendance call failed for tool={}, attempt={}: {}",
            toolName,
            attempt,
            ex.getMessage());
        synchronized (this) {
          closeClient();
        }
      }
    }
    throw new IllegalStateException("Attendance MCP service is unavailable.", last);
  }

  private Map<String, Object> decode(CallToolResult result) {
    if (result == null) {
      throw new IllegalStateException("Attendance MCP returned no result.");
    }
    if (Boolean.TRUE.equals(result.isError())) {
      String message =
          result.content() == null ? "MCP tool execution failed." : result.content().toString();
      throw new IllegalStateException(message);
    }
    if (result.content() == null || result.content().isEmpty()) {
      return Map.of();
    }

    List<Map<String, Object>> decoded = new ArrayList<>();
    for (Content content : result.content()) {
      if (content instanceof TextContent text && text.text() != null) {
        try {
          decoded.add(
              objectMapper.readValue(text.text(), new TypeReference<Map<String, Object>>() {}));
        } catch (Exception ex) {
          decoded.add(Map.of("text", text.text()));
        }
      }
    }
    if (decoded.isEmpty()) {
      return Map.of("content", result.content().toString());
    }
    if (decoded.size() == 1) {
      return decoded.get(0);
    }
    return Map.of("results", decoded);
  }

  private synchronized void closeClient() {
    if (client != null) {
      try {
        client.close();
      } catch (RuntimeException ex) {
        log.debug("MCP client close failed: {}", ex.getMessage());
      } finally {
        client = null;
      }
    }
  }

  @Override
  public void close() {
    closeClient();
  }
}
