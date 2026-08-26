package com.ai.shrija.leave.agent.config;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configures the MCP (Model Context Protocol) client that the Leave Agent
 * uses to reach external tool/resource servers — e.g. the company HRMS
 * exposing "is-holiday" and "get-employee-profile" (see mcp/LeaveMcpClient).
 *
 * Uses the Streamable HTTP client transport, which is the current
 * recommended transport for remote MCP servers — the older SSE client
 * transport is deprecated in SDK 2.x. mcp.hrms.url should point at the
 * server's MCP endpoint (e.g. http://localhost:9001/mcp).
 */
@Configuration
public class McpConfig {

    @Value("${mcp.hrms.url}")
    private String hrmsServerUrl;

    @Value("${mcp.hrms.request-timeout-seconds:10}")
    private long requestTimeoutSeconds;

    @Bean(destroyMethod = "close")
    public McpSyncClient mcpSyncClient() {
        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport
                .builder(hrmsServerUrl)
                .build();

        // Note: intentionally NOT calling client.initialize() here. This
        // bean method runs during Spring context startup, and initialize()
        // makes a blocking network call to the HRMS MCP server — if that
        // server is briefly unreachable, the whole leave-agent app would
        // fail to start. LeaveMcpClient performs a lazy, resilient
        // initialize-on-first-use instead, so a down HRMS server only
        // degrades the holiday/profile lookups, not the whole service.
        return McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(requestTimeoutSeconds))
                .clientInfo(new McpSchema.Implementation("leave-agent", "1.0.0"))
                .build();
    }
}
