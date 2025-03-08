package com.example.cdcagent.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class AgentStateManagerTest {

    private AgentStateManager stateManager;

    @BeforeEach
    void setUp() {
        stateManager = new AgentStateManager();
    }

    @Test
    void initialStateShouldBeStopped() {
        assertEquals(AgentStateManager.AgentState.STOPPED, stateManager.getState());
    }

    @Test
    void isRunning_shouldReturnTrueWhenStateIsRunning() {
        // 初期状態はSTOPPED
        assertFalse(stateManager.isRunning());
        
        // STARTINGに変更
        stateManager.setState(AgentStateManager.AgentState.STARTING);
        assertFalse(stateManager.isRunning());
        
        // RUNNINGに変更
        stateManager.setState(AgentStateManager.AgentState.RUNNING);
        assertTrue(stateManager.isRunning());
    }

    @Test
    void isPaused_shouldReturnTrueWhenStateIsPaused() {
        // 初期状態はSTOPPED
        assertFalse(stateManager.isPaused());
        
        // RUNNINGに変更
        stateManager.setState(AgentStateManager.AgentState.STARTING);
        stateManager.setState(AgentStateManager.AgentState.RUNNING);
        assertFalse(stateManager.isPaused());
        
        // PAUSEDに変更
        stateManager.setState(AgentStateManager.AgentState.PAUSED);
        assertTrue(stateManager.isPaused());
    }

    @Test
    void isStopping_shouldReturnTrueWhenStateIsStoppingOrStopped() {
        // 初期状態はSTOPPED
        assertTrue(stateManager.isStopping());
        
        // STARTINGに変更
        stateManager.setState(AgentStateManager.AgentState.STARTING);
        assertFalse(stateManager.isStopping());
        
        // STOPPINGに変更
        stateManager.setState(AgentStateManager.AgentState.STOPPING);
        assertTrue(stateManager.isStopping());
        
        // STOPPEDに変更
        stateManager.setState(AgentStateManager.AgentState.STOPPED);
        assertTrue(stateManager.isStopping());
    }

    @ParameterizedTest
    @MethodSource("validTransitions")
    void setState_shouldSucceedForValidTransitions(AgentStateManager.AgentState from, AgentStateManager.AgentState to) {
        // 初期状態はSTOPPEDのため、まずfromの状態に強制的に設定
        // リフレクションを使ってprivateフィールドを変更
        try {
            java.lang.reflect.Field stateField = AgentStateManager.class.getDeclaredField("state");
            stateField.setAccessible(true);
            java.util.concurrent.atomic.AtomicReference<AgentStateManager.AgentState> stateRef = 
                (java.util.concurrent.atomic.AtomicReference<AgentStateManager.AgentState>) stateField.get(stateManager);
            stateRef.set(from);
        } catch (Exception e) {
            fail("リフレクションによるstate設定に失敗: " + e.getMessage());
        }
        
        assertEquals(from, stateManager.getState());
        
        // 検証対象のトランジション
        boolean result = stateManager.setState(to);
        
        // 結果確認
        assertTrue(result, "トランジション " + from + " -> " + to + " は有効であるべき");
        assertEquals(to, stateManager.getState());
    }

    @ParameterizedTest
    @MethodSource("invalidTransitions")
    void setState_shouldFailForInvalidTransitions(AgentStateManager.AgentState from, AgentStateManager.AgentState to) {
        // 初期状態はSTOPPEDのため、まずfromの状態に強制的に設定
        // リフレクションを使ってprivateフィールドを変更
        try {
            java.lang.reflect.Field stateField = AgentStateManager.class.getDeclaredField("state");
            stateField.setAccessible(true);
            java.util.concurrent.atomic.AtomicReference<AgentStateManager.AgentState> stateRef = 
                (java.util.concurrent.atomic.AtomicReference<AgentStateManager.AgentState>) stateField.get(stateManager);
            stateRef.set(from);
        } catch (Exception e) {
            fail("リフレクションによるstate設定に失敗: " + e.getMessage());
        }
        
        assertEquals(from, stateManager.getState());
        
        // 検証対象のトランジション
        boolean result = stateManager.setState(to);
        
        // 結果確認
        assertFalse(result, "トランジション " + from + " -> " + to + " は無効であるべき");
        assertEquals(from, stateManager.getState()); // 状態は変わらないはず
    }

    // 有効な状態遷移のテストデータ
    static Stream<Arguments> validTransitions() {
        return Stream.of(
            Arguments.of(AgentStateManager.AgentState.STOPPED, AgentStateManager.AgentState.STARTING),
            Arguments.of(AgentStateManager.AgentState.STARTING, AgentStateManager.AgentState.RUNNING),
            Arguments.of(AgentStateManager.AgentState.STARTING, AgentStateManager.AgentState.STOPPING),
            Arguments.of(AgentStateManager.AgentState.RUNNING, AgentStateManager.AgentState.PAUSED),
            Arguments.of(AgentStateManager.AgentState.RUNNING, AgentStateManager.AgentState.STOPPING),
            Arguments.of(AgentStateManager.AgentState.PAUSED, AgentStateManager.AgentState.RUNNING),
            Arguments.of(AgentStateManager.AgentState.PAUSED, AgentStateManager.AgentState.STOPPING),
            Arguments.of(AgentStateManager.AgentState.STOPPING, AgentStateManager.AgentState.STOPPED)
        );
    }

    // 無効な状態遷移のテストデータ
    static Stream<Arguments> invalidTransitions() {
        return Stream.of(
            Arguments.of(AgentStateManager.AgentState.STOPPED, AgentStateManager.AgentState.RUNNING),
            Arguments.of(AgentStateManager.AgentState.STOPPED, AgentStateManager.AgentState.PAUSED),
            Arguments.of(AgentStateManager.AgentState.STOPPED, AgentStateManager.AgentState.STOPPING),
            Arguments.of(AgentStateManager.AgentState.STARTING, AgentStateManager.AgentState.PAUSED),
            Arguments.of(AgentStateManager.AgentState.RUNNING, AgentStateManager.AgentState.STARTING),
            Arguments.of(AgentStateManager.AgentState.PAUSED, AgentStateManager.AgentState.STARTING),
            Arguments.of(AgentStateManager.AgentState.STOPPING, AgentStateManager.AgentState.STARTING),
            Arguments.of(AgentStateManager.AgentState.STOPPING, AgentStateManager.AgentState.RUNNING),
            Arguments.of(AgentStateManager.AgentState.STOPPING, AgentStateManager.AgentState.PAUSED)
        );
    }
} 