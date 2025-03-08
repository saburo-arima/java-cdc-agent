package com.example.cdcagent.controller;

import com.example.cdcagent.service.AgentStateManager;
import com.example.cdcagent.service.CdcService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AgentControllerTest {

    @Mock
    private CdcService cdcService;

    @Mock
    private AgentStateManager stateManager;

    @InjectMocks
    private AgentController controller;

    @BeforeEach
    void setUp() {
    }

    @Test
    void status_shouldReturnCurrentState() {
        // モックの設定
        when(stateManager.getState()).thenReturn(AgentStateManager.AgentState.RUNNING);
        
        // 実行
        ResponseEntity<Map<String, Object>> response = controller.status();
        
        // 検証
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        assertEquals("RUNNING", body.get("state"));
    }

    @Test
    void start_shouldReturnSuccessWhenStateChanges() {
        // モックの設定
        when(stateManager.isStopping()).thenReturn(true);
        when(stateManager.getState()).thenReturn(AgentStateManager.AgentState.RUNNING);
        
        // 実行
        ResponseEntity<Map<String, Object>> response = controller.start();
        
        // 検証
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cdcService).start();
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        assertEquals("エージェントを起動しました", body.get("message"));
        assertEquals("RUNNING", body.get("state"));
    }

    @Test
    void start_shouldReturnConflictWhenAgentAlreadyRunning() {
        // モックの設定
        when(stateManager.isStopping()).thenReturn(false);
        when(stateManager.getState()).thenReturn(AgentStateManager.AgentState.RUNNING);
        
        // 実行
        ResponseEntity<Map<String, Object>> response = controller.start();
        
        // 検証
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(cdcService, never()).start();
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        assertEquals("エージェントはすでに起動しているか起動中です", body.get("message"));
        assertEquals("RUNNING", body.get("state"));
    }

    @Test
    void pause_shouldReturnSuccessWhenAgentIsRunning() {
        // モックの設定
        when(stateManager.isRunning()).thenReturn(true);
        when(stateManager.getState()).thenReturn(AgentStateManager.AgentState.PAUSED);
        
        // 実行
        ResponseEntity<Map<String, Object>> response = controller.pause();
        
        // 検証
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cdcService).pause();
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        assertEquals("エージェントを一時停止しました", body.get("message"));
        assertEquals("PAUSED", body.get("state"));
    }

    @Test
    void pause_shouldReturnConflictWhenAgentIsNotRunning() {
        // モックの設定
        when(stateManager.isRunning()).thenReturn(false);
        when(stateManager.getState()).thenReturn(AgentStateManager.AgentState.STOPPED);
        
        // 実行
        ResponseEntity<Map<String, Object>> response = controller.pause();
        
        // 検証
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(cdcService, never()).pause();
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        assertEquals("エージェントは実行中ではないため一時停止できません", body.get("message"));
        assertEquals("STOPPED", body.get("state"));
    }

    @Test
    void resume_shouldReturnSuccessWhenAgentIsPaused() {
        // モックの設定
        when(stateManager.isPaused()).thenReturn(true);
        when(stateManager.getState()).thenReturn(AgentStateManager.AgentState.RUNNING);
        
        // 実行
        ResponseEntity<Map<String, Object>> response = controller.resume();
        
        // 検証
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cdcService).resume();
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        assertEquals("エージェントを再開しました", body.get("message"));
        assertEquals("RUNNING", body.get("state"));
    }

    @Test
    void resume_shouldReturnConflictWhenAgentIsNotPaused() {
        // モックの設定
        when(stateManager.isPaused()).thenReturn(false);
        when(stateManager.getState()).thenReturn(AgentStateManager.AgentState.RUNNING);
        
        // 実行
        ResponseEntity<Map<String, Object>> response = controller.resume();
        
        // 検証
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(cdcService, never()).resume();
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        assertEquals("エージェントは一時停止中ではないため再開できません", body.get("message"));
        assertEquals("RUNNING", body.get("state"));
    }

    @Test
    void stop_shouldReturnSuccessWhenAgentIsNotStopped() {
        // モックの設定
        when(stateManager.isStopping()).thenReturn(false);
        when(stateManager.getState()).thenReturn(AgentStateManager.AgentState.STOPPED);
        
        // 実行
        ResponseEntity<Map<String, Object>> response = controller.stop();
        
        // 検証
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(cdcService).stop();
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("success", body.get("status"));
        assertEquals("エージェントを停止しました", body.get("message"));
        assertEquals("STOPPED", body.get("state"));
    }

    @Test
    void stop_shouldReturnConflictWhenAgentIsAlreadyStopped() {
        // モックの設定
        when(stateManager.isStopping()).thenReturn(true);
        when(stateManager.getState()).thenReturn(AgentStateManager.AgentState.STOPPED);
        
        // 実行
        ResponseEntity<Map<String, Object>> response = controller.stop();
        
        // 検証
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(cdcService, never()).stop();
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("error", body.get("status"));
        assertEquals("エージェントはすでに停止しているか停止中です", body.get("message"));
        assertEquals("STOPPED", body.get("state"));
    }
} 