package com.ai.shrija.manager;

import com.ai.shrija.manager.agent.ManagerAgent;
import com.google.adk.web.AdkWebServer;

public class ManagerApplication {

  public static void main(String[] args) {
    //   SpringApplication.run(ManagerApplication.class, args);

    AdkWebServer.start(ManagerAgent.ROOT_AGENT);
  }
}
