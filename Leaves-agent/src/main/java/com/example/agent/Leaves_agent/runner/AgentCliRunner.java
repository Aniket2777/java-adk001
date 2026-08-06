package com.example.agent.Leaves_agent.runner;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.example.agent.Leaves_agent.agent.ManagerAgent;
import com.google.adk.agents.RunConfig;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.reactivex.rxjava3.core.Flowable;
import java.util.Scanner;

public class AgentCliRunner {

  public static void main(String[] args) {
    RunConfig runConfig = RunConfig.builder().build();
    InMemoryRunner runner = new InMemoryRunner(ManagerAgent.ROOT_AGENT);

    Session session =
        runner.sessionService().createSession(runner.appName(), "local-user").blockingGet();

    System.out.println("Enterprise agent ready. Type 'quit' to exit.");

    try (Scanner scanner = new Scanner(System.in, UTF_8)) {
      while (true) {
        System.out.print("\nYou > ");
        String userInput = scanner.nextLine();
        if ("quit".equalsIgnoreCase(userInput)) {
          break;
        }

        Content userMsg = Content.fromParts(Part.fromText(userInput));
        Flowable<Event> events =
            runner.runAsync(session.userId(), session.id(), userMsg, runConfig);

        System.out.print("\nAgent > ");
        events.blockingForEach(
            event -> {
              if (event.finalResponse()) {
                System.out.println(event.stringifyContent());
              }
            });
      }
    }
  }
}
