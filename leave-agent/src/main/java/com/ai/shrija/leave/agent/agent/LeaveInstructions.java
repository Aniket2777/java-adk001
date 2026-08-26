package com.ai.shrija.leave.agent.agent;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Loads the LeaveAgent's system prompt from
 * resources/prompts/leave-agent.txt so the instructions can be edited
 * without recompiling code.
 */
@Component
public class LeaveInstructions {

    private static final String PROMPT_PATH = "classpath:prompts/leave-agent.txt";

    private final String instructions;

    public LeaveInstructions(ResourcePatternResolver resourceResolver) throws IOException {
        Resource resource = resourceResolver.getResource(PROMPT_PATH);
        try (var inputStream = resource.getInputStream()) {
            this.instructions = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    public String get() {
        return instructions;
    }
}
