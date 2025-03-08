package com.example.cdcagent.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ChangeEventTest {

    @Test
    void createEmptyChangeEvent_shouldSetDefaultValues() {
        // 実行
        ChangeEvent event = new ChangeEvent();
        
        // 検証
        assertNull(event.getId());
        assertNull(event.getType());
        assertNull(event.getDatabase());
        assertNull(event.getTable());
        assertNull(event.getOperation());
        assertNull(event.getTimestamp());
        assertNull(event.getBefore());
        assertNull(event.getAfter());
    }

    @Test
    void createChangeEventWithConstructor_shouldSetAllFieldsCorrectly() {
        // テストデータ
        String id = UUID.randomUUID().toString();
        String type = "mysql";
        String database = "testdb";
        String table = "testtable";
        ChangeEvent.OperationType operation = ChangeEvent.OperationType.UPDATE;
        Instant timestamp = Instant.now();
        
        Map<String, Object> before = new HashMap<>();
        before.put("id", 1);
        before.put("name", "Old Name");
        
        Map<String, Object> after = new HashMap<>();
        after.put("id", 1);
        after.put("name", "New Name");
        
        // 実行
        ChangeEvent event = new ChangeEvent(id, type, database, table, operation, timestamp, before, after);
        
        // 検証
        assertEquals(id, event.getId());
        assertEquals(type, event.getType());
        assertEquals(database, event.getDatabase());
        assertEquals(table, event.getTable());
        assertEquals(operation, event.getOperation());
        assertEquals(timestamp, event.getTimestamp());
        assertEquals(before, event.getBefore());
        assertEquals(after, event.getAfter());
    }

    @Test
    void setAndGetProperties_shouldWorkCorrectly() {
        // テストデータ
        String id = UUID.randomUUID().toString();
        String type = "mysql";
        String database = "testdb";
        String table = "testtable";
        ChangeEvent.OperationType operation = ChangeEvent.OperationType.DELETE;
        Instant timestamp = Instant.now();
        
        Map<String, Object> before = new HashMap<>();
        before.put("id", 1);
        before.put("name", "Test Name");
        
        // 実行
        ChangeEvent event = new ChangeEvent();
        event.setId(id);
        event.setType(type);
        event.setDatabase(database);
        event.setTable(table);
        event.setOperation(operation);
        event.setTimestamp(timestamp);
        event.setBefore(before);
        
        // 検証
        assertEquals(id, event.getId());
        assertEquals(type, event.getType());
        assertEquals(database, event.getDatabase());
        assertEquals(table, event.getTable());
        assertEquals(operation, event.getOperation());
        assertEquals(timestamp, event.getTimestamp());
        assertEquals(before, event.getBefore());
        assertNull(event.getAfter());
    }

    @Test
    void toString_shouldIncludeAllRelevantFields() {
        // テストデータ
        String id = "test-id";
        String type = "mysql";
        String database = "testdb";
        String table = "testtable";
        ChangeEvent.OperationType operation = ChangeEvent.OperationType.INSERT;
        Instant timestamp = Instant.now();
        
        // 実行
        ChangeEvent event = new ChangeEvent();
        event.setId(id);
        event.setType(type);
        event.setDatabase(database);
        event.setTable(table);
        event.setOperation(operation);
        event.setTimestamp(timestamp);
        
        String result = event.toString();
        
        // 検証
        assertTrue(result.contains(id));
        assertTrue(result.contains(type));
        assertTrue(result.contains(database));
        assertTrue(result.contains(table));
        assertTrue(result.contains(operation.toString()));
        assertTrue(result.contains(timestamp.toString()));
    }

    @Test
    void operationType_shouldHaveThreeValues() {
        // 検証
        assertEquals(3, ChangeEvent.OperationType.values().length);
        assertEquals(ChangeEvent.OperationType.INSERT, ChangeEvent.OperationType.valueOf("INSERT"));
        assertEquals(ChangeEvent.OperationType.UPDATE, ChangeEvent.OperationType.valueOf("UPDATE"));
        assertEquals(ChangeEvent.OperationType.DELETE, ChangeEvent.OperationType.valueOf("DELETE"));
    }
} 