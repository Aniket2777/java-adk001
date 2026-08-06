package com.example.agent.Leaves_agent.config;

import io.github.cdimascio.dotenv.Dotenv;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Loads DB connection details from environment variables (via a local .env file for development).
 * Nothing sensitive is hardcoded in source, so this class is safe to commit to version control.
 *
 * <p>Required variables — see .env.example: DB_URL, DB_USER, DB_PASSWORD
 */
public final class DatabaseConfig {

  private static final Dotenv DOTENV =
      Dotenv.configure()
          .ignoreIfMissing() // falls back to real env vars (e.g. in CI/prod) if no .env file exists
          .load();

  private DatabaseConfig() {}

  public static Connection getConnection() throws SQLException {
    String url = require("DB_URL");
    String user = require("DB_USER");
    String password = require("DB_PASSWORD");
    return DriverManager.getConnection(url, user, password);
  }

  private static String require(String key) {
    String value = DOTENV.get(key, System.getenv(key));
    if (value == null || value.isBlank()) {
      throw new IllegalStateException(
          "Missing required environment variable: "
              + key
              + ". Copy .env.example to .env and fill it in.");
    }
    return value;
  }
}
