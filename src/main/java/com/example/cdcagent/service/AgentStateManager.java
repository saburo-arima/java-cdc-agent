package com.example.cdcagent.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicReference;

/**
 * エージェントの状態を管理するサービス
 */
@Service
public class AgentStateManager {
    private static final Logger logger = LoggerFactory.getLogger(AgentStateManager.class);

    public enum AgentState {
        STARTING, RUNNING, PAUSED, STOPPING, STOPPED
    }

    private final AtomicReference<AgentState> state = new AtomicReference<>(AgentState.STOPPED);

    /**
     * エージェントの現在の状態を取得
     */
    public AgentState getState() {
        return state.get();
    }

    /**
     * エージェントの状態を変更
     * @param newState 新しい状態
     * @return 状態変更に成功した場合true
     */
    public boolean setState(AgentState newState) {
        AgentState currentState = state.get();
        
        // 状態遷移の検証
        if (!isValidTransition(currentState, newState)) {
            logger.warn("無効な状態遷移: {} -> {}", currentState, newState);
            return false;
        }
        
        boolean success = state.compareAndSet(currentState, newState);
        if (success) {
            logger.info("エージェント状態変更: {} -> {}", currentState, newState);
        }
        return success;
    }

    /**
     * エージェントが実行中かどうかを確認
     */
    public boolean isRunning() {
        return state.get() == AgentState.RUNNING;
    }

    /**
     * エージェントが一時停止中かどうかを確認
     */
    public boolean isPaused() {
        return state.get() == AgentState.PAUSED;
    }

    /**
     * エージェントが停止中または停止しているかどうかを確認
     */
    public boolean isStopping() {
        AgentState currentState = state.get();
        return currentState == AgentState.STOPPING || currentState == AgentState.STOPPED;
    }

    /**
     * 状態遷移が有効かどうかを検証
     */
    private boolean isValidTransition(AgentState from, AgentState to) {
        switch (from) {
            case STOPPED:
                return to == AgentState.STARTING;
            case STARTING:
                return to == AgentState.RUNNING || to == AgentState.STOPPING;
            case RUNNING:
                return to == AgentState.PAUSED || to == AgentState.STOPPING;
            case PAUSED:
                return to == AgentState.RUNNING || to == AgentState.STOPPING;
            case STOPPING:
                return to == AgentState.STOPPED;
            default:
                return false;
        }
    }
} 