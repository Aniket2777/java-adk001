package com.ai.shrija.leave.agent.mcp;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Thin wrapper around the MCP sync client configured in config/McpConfig.java.
 * Gives the Leave Agent access to external MCP-exposed tools/resources such
 * as an HRMS (HR Management System) or holiday-calendar server, without the
 * rest of the codebase needing to know about the MCP SDK's call-tool
 * request/response shapes.
 *
 * Initialization of the underlying session happens lazily, on first use, and
 * every public method degrades gracefully (logs + safe default) instead of
 * throwing if the HRMS MCP server is unreachable, slow, or returns an error
 * result — a leave application should never fail just because the holiday
 * calendar lookup timed out.
 */
@Component
public class LeaveMcpClient {

    private static final Logger log = LoggerFactory.getLogger(LeaveMcpClient.class);

    private final McpSyncClient mcpSyncClient;
    private volatile boolean initialized = false;

    public LeaveMcpClient(McpSyncClient mcpSyncClient) {
        this.mcpSyncClient = mcpSyncClient;
    }

    /**
     * Looks up whether a given date is a company holiday via the external
     * HRMS MCP server's "is-holiday" tool. Returns false (i.e. "not a known
     * holiday") if the HRMS server can't be reached, so callers should treat
     * a false result as "unknown or not a holiday", not a guarantee.
     */
    public boolean isCompanyHoliday(String isoDate) {
        McpSchema.CallToolResult result = callTool("is-holiday", Map.of("date", isoDate));
        if (result == null || (result.isError() != null && result.isError())) {
            return false;
        }
        return result.content().stream()
                .filter(c -> c instanceof McpSchema.TextContent)
                .map(c -> ((McpSchema.TextContent) c).text())
                .anyMatch(text -> Boolean.parseBoolean(text.trim()));
    }

    /**
     * Fetches an employee's HR record snapshot (department, manager, tenure,
     * etc.) from the HRMS MCP server's "get-employee-profile" tool so the
     * agent can reason about policy exceptions (e.g. probation periods).
     * Returns "{}" if the profile can't be retrieved.
     */
    public String getEmployeeProfile(String employeeId) {
        McpSchema.CallToolResult result = callTool("get-employee-profile", Map.of("employeeId", employeeId));
        if (result == null) {
            return "{}";
        }
        return result.content().stream()
                .filter(c -> c instanceof McpSchema.TextContent)
                .map(c -> ((McpSchema.TextContent) c).text())
                .findFirst()
                .orElse("{}");
    }

    /**
     * True once the MCP session has been successfully established. Useful
     * for a health/readiness endpoint if one is added later.
     */
    public boolean isReady() {
        return initialized;
    }

    private McpSchema.CallToolResult callTool(String toolName, Map<String, Object> arguments) {
        try {
            ensureInitialized();
            return mcpSyncClient.callTool(new McpSchema.CallToolRequest(toolName, arguments));
        } catch (Exception ex) {
            log.warn("MCP call to HRMS tool '{}' failed; degrading gracefully: {}", toolName, ex.getMessage());
            return null;
        }
    }

    private void ensureInitialized() {
        if (initialized) {
            return;
        }
        synchronized (this) {
            if (!initialized) {
                mcpSyncClient.initialize();
                initialized = true;
                log.info("MCP session with HRMS server established.");
            }
        }
    }
}
