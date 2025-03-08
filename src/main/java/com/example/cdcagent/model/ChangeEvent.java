package com.example.cdcagent.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;

/**
 * CDCによって検出された変更イベントを表すモデル
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChangeEvent {

    private String id;
    private String type;
    private String database;
    private String table;
    private OperationType operation;
    private Instant timestamp;
    private Map<String, Object> before;
    private Map<String, Object> after;

    public enum OperationType {
        INSERT, UPDATE, DELETE
    }

    public ChangeEvent() {
    }

    public ChangeEvent(String id, String type, String database, String table, OperationType operation,
                       Instant timestamp, Map<String, Object> before, Map<String, Object> after) {
        this.id = id;
        this.type = type;
        this.database = database;
        this.table = table;
        this.operation = operation;
        this.timestamp = timestamp;
        this.before = before;
        this.after = after;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public OperationType getOperation() {
        return operation;
    }

    public void setOperation(OperationType operation) {
        this.operation = operation;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, Object> getBefore() {
        return before;
    }

    public void setBefore(Map<String, Object> before) {
        this.before = before;
    }

    public Map<String, Object> getAfter() {
        return after;
    }

    public void setAfter(Map<String, Object> after) {
        this.after = after;
    }

    @Override
    public String toString() {
        return "ChangeEvent{" +
                "id='" + id + '\'' +
                ", type='" + type + '\'' +
                ", database='" + database + '\'' +
                ", table='" + table + '\'' +
                ", operation=" + operation +
                ", timestamp=" + timestamp +
                '}';
    }
} 