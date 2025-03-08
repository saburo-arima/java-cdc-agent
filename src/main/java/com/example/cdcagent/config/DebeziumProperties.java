package com.example.cdcagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

/**
 * Debezium設定のプロパティクラス
 */
@Component
@ConfigurationProperties(prefix = "debezium")
public class DebeziumProperties {

    private final Connector connector = new Connector();
    private final Source source = new Source();

    public Connector getConnector() {
        return connector;
    }

    public Source getSource() {
        return source;
    }

    public static class Connector {
        private String name;
        private Map<String, String> properties = new HashMap<>();

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Map<String, String> getProperties() {
            return properties;
        }

        public void setProperties(Map<String, String> properties) {
            this.properties = properties;
        }
    }

    public static class Source {
        private final Database database = new Database();

        public Database getDatabase() {
            return database;
        }

        public static class Database {
            private String hostname;
            private int port;
            private String user;
            private String password;
            private int serverId;
            private String serverName;
            private boolean includeSchemaChanges;

            public String getHostname() {
                return hostname;
            }

            public void setHostname(String hostname) {
                this.hostname = hostname;
            }

            public int getPort() {
                return port;
            }

            public void setPort(int port) {
                this.port = port;
            }

            public String getUser() {
                return user;
            }

            public void setUser(String user) {
                this.user = user;
            }

            public String getPassword() {
                return password;
            }

            public void setPassword(String password) {
                this.password = password;
            }

            public int getServerId() {
                return serverId;
            }

            public void setServerId(int serverId) {
                this.serverId = serverId;
            }

            public String getServerName() {
                return serverName;
            }

            public void setServerName(String serverName) {
                this.serverName = serverName;
            }

            public boolean isIncludeSchemaChanges() {
                return includeSchemaChanges;
            }

            public void setIncludeSchemaChanges(boolean includeSchemaChanges) {
                this.includeSchemaChanges = includeSchemaChanges;
            }
        }
    }
} 